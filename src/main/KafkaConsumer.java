package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;  // This is your DTO for incoming Kafka messages
import com.jpmc.midascore.entity.UserRecord;       // This is your @Entity for users
import com.jpmc.midascore.entity.TransactionRecord; // ✅ FIXED: import your TransactionRecord entity
import com.jpmc.midascore.repository.UserRepository; 
import com.jpmc.midascore.repository.TransactionRecordRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public KafkaConsumer(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @KafkaListener(
        topics = "${general.kafka-topic}",
        groupId = "midas-core-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void listen(Transaction transaction) {
        logger.info("✅ Received transaction: {}", transaction);

        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender == null || recipient == null) {
            logger.warn("❌ Invalid sender or recipient");
            return;
        }

        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("❌ Insufficient funds");
            return;
        }

        // Adjust balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);
        UserRecord waldorf = userRepository.findByName("Waldorf");
        logger.info("📊 Waldorf final balance: {}", waldorf.getBalance());
        // Save the transaction record
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(transaction.getAmount());
        transactionRecordRepository.save(record);

        logger.info("✅ Transaction processed & saved");
    }
}
