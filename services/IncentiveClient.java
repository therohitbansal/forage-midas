@Service
public class IncentiveClient {
    private final RestTemplate restTemplate;
    
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";
    
    public IncentiveClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    public BigDecimal getIncentiveAmount(Transaction transaction) {
        IncentiveResponse response = restTemplate.postForObject(
            INCENTIVE_API_URL, 
            transaction, 
            IncentiveResponse.class
        );
        return response != null ? response.getAmount() : BigDecimal.ZERO;
    }
    
    // Response DTO
    private static class IncentiveResponse {
        private BigDecimal amount;
        
        // getter and setter
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}