package platform;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.*;
import java.time.*;

@Entity
public class Code {

    @Id
    private UUID id;
    private String code;
    private LocalDateTime date;
    private long time;
    private long views;
    private LocalDateTime expiryDate;
   
    // constructors
    Code() {} 
    
    Code(UUID id, String code, LocalDateTime date, long time, long views, LocalDateTime expiryDate) {
        this.id = id;
        this.code = code;
        this.date = date;
        this.time = time;
        this.views = views;
        this.expiryDate = expiryDate;
    }
    // getters and setters
    UUID getId() {
        return id;
    }

    String getCode() {
        return code;
    }

    LocalDateTime getDate() {
        return date;
    }
  
    long getTime() {
        return time;
    }

    long getViews() {
        return views;
    }
    
    LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    void setId(UUID id) {
        this.id = id;
    }

    void setCode(String code) {
        this.code = code;
    }
    
    void setDate(LocalDateTime date) {
        this.date = date;
    }

    void setTime(long time) {
        this.time = time;
    }

    void setViews(long views) {
        this.views = views;
    }

    void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        String dateStr =  new FormatDateTime(date).getFormatDateTime();
        String expiryDateStr = new FormatDateTime(expiryDate).getFormatDateTime();
        return String.format("Code[id=%s, code='%s', date='%s', time=%d, views=%d, expiryDate=%s]", id.toString(), code, dateStr, time, views, expiryDateStr);
    }
}