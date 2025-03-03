package BackAnt.service.kafka;

import BackAnt.entity.AccessLog;
import BackAnt.service.AccessLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaConsumerService {

    private final AccessLogService accessLogService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "access-log-topic", groupId = "access-log-group")
    public void consumeMessage(String message) {
        try {
            System.out.println("Message received from Kafka: " + message);
    
            // JSON 메시지를 AccessLog 객체로 변환
            AccessLog log = parseMessage(message);
            System.out.println("Parsed log: " + log);
    
            // MongoDB에 저장
            accessLogService.saveLog(log);
            System.out.println("Log saved to MongoDB: " + log);
        } catch (Exception e) {
            System.err.println("Kafka Consumer Error: " + e.getMessage());
    
            // Kafka 장애 발생 시 MongoDB에 직접 저장
            try {
                AccessLog log = parseMessage(message);
                log.setSavedFrom("MongoDB (Kafka Failed)"); // 실패 시 별도 필드 추가
                accessLogService.saveLog(log);
                System.out.println("Kafka failed, log saved to MongoDB directly: " + log);
            } catch (Exception ex) {
                System.err.println("MongoDB Save Error: " + ex.getMessage());
            }
        }
    }


    private AccessLog parseMessage(String message) {
        try {
            return objectMapper.readValue(message, AccessLog.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Kafka message", e);
        }
    }
}
