package platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController
public class CodeSharingPlatform {

    static Res res = new Res();
    static String inputHtml;
    static String displayHtml;
    static String display2Html;

    @Autowired
	private CodeService cs;
    
    public static void main(String[] args) {
        SpringApplication.run(CodeSharingPlatform.class, args);
        inputHtml = ReadText.readAll("input.html");
        displayHtml = ReadText.readAll("display.html");
        display2Html = ReadText.readAll("display2.html");
//        File file = new File("input.html");
//        System.out.println(file.getAbsolutePath());
    }

    List<Data> getLatest(CodeService cs) {
        List<Data> latestList = new ArrayList<>();
        List<Code> codeList = cs.findCode();
        int j = codeList.size() - 1;
        for (int i = 0; j >= 0 && i < 10; j--) {
            Code code = codeList.get(j);
            if (code.getTime() >= 0 || code.getViews() >= 0) {
                continue;
            }
            String date = new FormatDateTime(code.getDate()).getFormatDateTime();
            latestList.add(new Data(code.getCode(), date, 0, 0));
            i++;
        }
        return latestList;
    }
  
    String getLatestHtml(CodeService cs) {
        StringBuilder sb = new StringBuilder();
        List<Data> latestList = getLatest(cs);
        for (Data data: latestList) {
            sb.append(String.format("<span class='load_date'>%s</span>", data.getDate()));
            sb.append(String.format("<pre class='code_snippet'><code>%s</code></pre>", data.getCode()));
        }
        return String.format(display2Html, sb.toString());
    }

    Data returnFromId(String id) {
        UUID uuid = UUID.fromString(id);
        Optional<Code> codeNullable = cs.findCode(uuid);
        if (!codeNullable.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "");   
        }
        Code code = codeNullable.get();
        long views = code.getViews();
        if (views >= 0L) {
            views--;
            if (views < 0L) {
                cs.delete(code.getId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "");
            }
            code.setViews(views);
            cs.save(code);
        }
        long time = code.getTime();
        if (time >= 0L) {
            LocalDateTime now = LocalDateTime.now();
            if (now.compareTo(code.getExpiryDate()) > 0L) {
                cs.delete(code.getId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "");
            } else {
                Duration duration = Duration.between(now, code.getExpiryDate());
                time = duration.getSeconds();
                code.setTime(time);
                cs.save(code);
            }          
        } 
        String date = new FormatDateTime(code.getDate()).getFormatDateTime();
        return new Data(code.getCode(), date, time, views);   
    }

    @GetMapping("/api/code/latest") 
    public List<Data> latestJsonReturn() {
        List<Data> latestList = getLatest(cs);
        return latestList;
    }

    @GetMapping("/code/latest") 
    public String latestTextReturn() {
        return getLatestHtml(cs);
    }

    @GetMapping("/code/{id}") 
    public String textReturn(@PathVariable String id) {
        Data data = returnFromId(id);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<span id='load_date'>%s</span><br>", data.getDate()));
        if (data.getViews() >= 0L) {
            sb.append(String.format("<span id='views_restriction'>%s more views allowed</span><br>", data.getViews()));
        }
        if (data.getTime() >= 0L) {
            sb.append(String.format("<span id='time_restriction'>The code will be available for %s seconds</span>", data.getTime()));
        }
        sb.append(String.format("<pre id='code_snippet'><code>%s</code></pre>", data.getCode()));
    
        return String.format(displayHtml, sb.toString());
    }

    @GetMapping("/api/code/{id}") 
    public Data jsonReturn(@PathVariable String id) {
        Data data = returnFromId(id);
        if (data.getViews() == -1L) {
            data.setViews(0L);
        } 
        if (data.getTime() == -1L) {
            data.setTime(0L);
        } 
        return data;
    }

    @GetMapping("/code/new") 
    public String inputHtmlReturn() {
        return inputHtml;
    }

    @PostMapping(value = "/api/code/new", consumes = "application/json") 
    public Res getJson(@RequestBody Data data) {
        UUID id = UUID.randomUUID();

        long time = data.getTime() > 0L ? data.getTime() : -1L;
        long views = data.getViews() > 0L ? data.getViews() : -1L; 

        LocalDateTime now = LocalDateTime.now(); 
        LocalDateTime expiryDate = now.plusSeconds((long) time);
        if (time == -1) {
            expiryDate = now;
        } 
    
        Code code = new Code(id, data.getCode(), now, time, views, expiryDate);
        cs.save(code);
        res.setId(id);
//        System.out.println(code.toString());
        return res;
    }
}

class Res {

    private String id;

    public Res() {}

    public void setId(UUID uuid) {
        id = uuid.toString();
    }

    public String getId() {
        return id;
    }
}


class FormatDateTime {

    private static final String DATE_FORMATTER= "yyyy-MM-dd HH:mm:ss";
    private static String formatDateTime;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);

    FormatDateTime(LocalDateTime date) {
        formatDateTime = date.format(formatter);
    }

    String getFormatDateTime() {
        return formatDateTime;
    }

    static LocalDateTime toLocalDateTime(String formatDate) {
        return LocalDateTime.parse(formatDate, formatter);
    }
}

class Data {
 
    private String code;
    private String date;
    private long time;
    private long views;

    public Data() {}

    public Data(String code, String date, long time, long views) {
        this.code = code;
        this.date = date;
        this.time = time;
        this.views = views;
    }
       
	public void setCode(String code) {
        this.code = code;
	}    
      
	public void setSate(String date) {
        this.date = date;
	}    
      
	public void setTime(long time) {
        this.time = time;
	}    
      
	public void setViews(long views) {
        this.views = views;
	}    

	public String getCode() {
		return code;
    } 
      
	public String getDate() {
		return date;
    } 
      
	public long getTime() {
		return time;
    } 
      
	public long getViews() {
		return views;
    } 
    
    @Override
    public String toString() {
        return String.format("Data[code='%s', date='%s', time='%d', views='%d']", code, date, time, views);
    }
}

class ReadText {

    static String readAll(String fileName) {
        StringBuilder str = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String rec = "";
            while ((rec = br.readLine()) != null) {
                str.append(rec);
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return str.toString();
    }
}