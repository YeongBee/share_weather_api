package com.yeongbee.weather.my;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherShtService {

    private final WeatherApiIntegratedService integratedService;

    @Getter
    private final WeatherUSN weatherUsn = new WeatherUSN();


    private final List<WeatherForecastAPI> weatherForecastList = new ArrayList<>();

    @Value("${weather.sht.api}")
    private String serviceKey;

    @Value("${weahter.sht.vilage.url}")
    private String vilageUrl;

    @Value("${weahter.sht.usn.url}")
    private String usnUrl;


    @PostConstruct
    public void init() throws ParseException, IOException, IllegalAccessException {
        firstInit();
        usnInputData();
    }

    // TODO 20241203 리팩토링
    public List<WeatherSetEntity> getWeatherShtSet() {

        LocalDateTime localDateTime = LocalDateTime.now();

        List<WeatherSetEntity> weatherList = new ArrayList<>();
        List<WeatherForecastAPI> getLists = new ArrayList<>();

        String nextDay = localDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String afterTomorrow = localDateTime.plusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String afterTomorrow2 = localDateTime.plusDays(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String afterTomorrow3 = localDateTime.plusDays(4).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (WeatherForecastAPI weatherForecastAPI : weatherForecastList) {
            String forecastDate = weatherForecastAPI.getFcstDate();
            String forecastTime = weatherForecastAPI.getFcstTime();

            if ((forecastDate.equals(nextDay) && (forecastTime.equals("0600") || forecastTime.equals("1500"))) ||
                    (forecastDate.equals(afterTomorrow) && (forecastTime.equals("0600") || forecastTime.equals("1500"))) ||
                    (forecastDate.equals(afterTomorrow2) && (forecastTime.equals("0600") || forecastTime.equals("1500")))) {
                getLists.add(weatherForecastAPI);


            }
            LocalTime currentTime = LocalTime.now();

            LocalTime sixPM = LocalTime.of(18, 0);
            if (currentTime.isAfter(sixPM)) {
                if (forecastDate.equals(afterTomorrow3) && (forecastTime.equals("0600") || forecastTime.equals("1500"))) {
                    getLists.add(weatherForecastAPI);
                }
                if (getLists.size() == 8) {
                    break;
                }
            } else {
                if (getLists.size() == 6) {
                    break;
                }
            }
        }

        //TODO
/*        for (WeatherForecastAPI getList : getLists) {
            log.warn("getList = {}",getList.toString());
        }*/


        weatherList.add(new WeatherSetEntity(
                Long.parseLong(getLists.get(0).getPOP()), // rnStAm
                Long.parseLong(getLists.get(1).getPOP()), // rnStPm
                (long) Double.parseDouble(getLists.get(0).getTMN()), // taMin
                (long) Double.parseDouble(getLists.get(1).getTMX()), // taMax
                getLists.get(0).getSKY().equals("1") ? "맑음" : "흐림",
                getLists.get(1).getSKY().equals("1") ? "맑음" : "흐림"
        ));

        weatherList.add(new WeatherSetEntity(
                Long.parseLong(getLists.get(2).getPOP()), // rnStAm
                Long.parseLong(getLists.get(3).getPOP()), // rnStPm
                (long) Double.parseDouble(getLists.get(2).getTMN()), // taMin
                (long) Double.parseDouble(getLists.get(3).getTMX()), // taMax
                getLists.get(2).getSKY().equals("1") ? "맑음" : "흐림", // wfAm
                getLists.get(3).getSKY().equals("1") ? "맑음" : "흐림" // wfPm
        ));


        // 06시 이상 18시 미만이면 데이터를 추가로 넣는다
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(6, 0);
        LocalTime endTime = LocalTime.of(18, 0);


        if (currentTime.isBefore(startTime) && currentTime.isAfter(endTime)) {
            weatherList.add(new WeatherSetEntity(
                    Long.parseLong(getLists.get(4).getPOP()), // rnStAm
                    Long.parseLong(getLists.get(5).getPOP()), // rnStPm
                    (long) Double.parseDouble(getLists.get(4).getTMN()), // taMin
                    (long) Double.parseDouble(getLists.get(5).getTMX()), // taMax
                    getLists.get(4).getSKY().equals("1") ? "맑음" : "흐림", // wfAm
                    getLists.get(5).getSKY().equals("1") ? "맑음" : "흐림" // wfPm
            ));
        }

        return weatherList;
    }


    public List<WeatherForecastAPI> getShtFiveWeather() {
        LocalDateTime localDateTime = LocalDateTime.now();

        String oneHour = localDateTime.plusHours(1).format(DateTimeFormatter.ofPattern("HHmm")).substring(0, 2) + "00";

        List<WeatherForecastAPI> fiveWeather = new ArrayList<>();

        int index = IntStream.range(0, weatherForecastList.size())
                .filter(i -> weatherForecastList.get(i).getFcstTime().equals(oneHour))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("리스트 데이터가 없습니다"));

        fiveWeather.add(weatherForecastList.get(index));
        fiveWeather.add(weatherForecastList.get(index + 1));
        fiveWeather.add(weatherForecastList.get(index + 3));
        fiveWeather.add(weatherForecastList.get(index + 5));
        fiveWeather.add(weatherForecastList.get(index + 7));
        fiveWeather.add(weatherForecastList.get(index + 9));

        return fiveWeather;
    }


    @Scheduled(cron = "0 41 * * * ?")
    private void usnInputData() {

        LocalDateTime localDateTime = LocalDateTime.now();

//        String baseTime = localDateTime.minusMinutes(1).format(DateTimeFormatter.ofPattern("HHmm"));
        String baseTime = localDateTime.format(DateTimeFormatter.ofPattern("HHmm"));
        try {

            JSONArray jsonData = integratedService.getWeatherJsonArray(SetUrl(baseTime, usnUrl, localDateTime));

            for (Object jsonDat : jsonData) {
                JSONObject itemObject = (JSONObject) jsonDat;
                String category = (String) itemObject.get("category");
                String value = (String) itemObject.get("obsrValue");
                weatherUsn.setLocalDateTime(localDateTime);
                updateEntityFromJsonObject(weatherUsn, category, value);

            }
        } catch (NullPointerException e) {
            log.error("초단기 실황 데이터는 0 ~ 6분 사이 데이터를 제공하지 않습니다.");
            log.warn("1분후 다시 시작");
            retryUsnInputData();
        }

        log.info("weatherUsn Update : {}", baseTime);
//        log.info("weatherUsn={}", weatherUsn);

    }

    @SneakyThrows
    private void retryUsnInputData() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::usnInputData, 1, TimeUnit.MINUTES);
    }


    // 처음 시작시 데이터가 없으므로 init을 해서 API 데이터를 가져옴
    private void firstInit() throws ParseException, IOException, IllegalAccessException {

        LocalDateTime localDateTime = LocalDateTime.now();

        int currentHour = localDateTime.getHour();

        String baseDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime;

        if (currentHour < 2) {
            baseDate = localDateTime.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        } else if (currentHour < 5) {
            baseTime = "0200";
        } else if (currentHour < 8) {
            baseTime = "0500";
        } else if (currentHour < 11) {
            baseTime = "0800";
        } else if (currentHour < 14) {
            baseTime = "1100";
        } else if (currentHour < 17) {
            baseTime = "1400";
        } else if (currentHour < 20) {
            baseTime = "1700";
        } else if (currentHour < 23) {
            baseTime = "2000";
        } else {
            baseTime = "2300";
        }

        log.info("ShtBaseTime : {}", baseTime);
//        baseTime="1100";

        String urlParams = "?serviceKey=" + URLEncoder.encode(serviceKey, "UTF-8")
                + "&pageNo=1&numOfRows=1000&dataType=JSON&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=64&ny=123";

        String weatherJson = integratedService.getWeatherJson(urlParams, vilageUrl);


        JSONArray jsonData = integratedService.getWeatherJsonArray(weatherJson);
        JSONObject itemObject;

        for (Object data : jsonData) {
            itemObject = (JSONObject) data;
            String forcastDate = (String) itemObject.get("fcstDate");
            String forcastTime = (String) itemObject.get("fcstTime");
            String category = (String) itemObject.get("category");
            String value = (String) itemObject.get("fcstValue");
            WeatherForecastAPI weatherForecastAPI = findExistingForecast(forcastDate, forcastTime);
            updateEntityFromJsonObject(weatherForecastAPI, category, value);
        }

//        for (WeatherForecastAPI weatherForecastAPI : weatherForecastList) {
//            log.info("weatherForecastAPI : {}", weatherForecastAPI);
//        }

        log.info("weatherShtDate={}  weatherShtTime={}", baseDate, baseTime);
//        log.info("WeatherShtService : update Size={}", weatherForecastList.size());
    }


    // 정해진 시간마다 api를 불러옴
    @Scheduled(cron = "00 11 2,5,8,11,14,17,20,23 * * ?")
    private void inputData() {

        LocalDateTime localDateTime = LocalDateTime.now();
        String baseTime = localDateTime.format(DateTimeFormatter.ofPattern("HHmm")).substring(0, 2) + "00";

        weatherForecastList.clear();

        try {

            JSONArray jsonData = integratedService.getWeatherJsonArray(SetUrl(baseTime, vilageUrl, localDateTime));

            for (Object data : jsonData) {

                JSONObject itemObject = (JSONObject) data;
                String forcastDate = (String) itemObject.get("fcstDate");
                String forcastTime = (String) itemObject.get("fcstTime");
                String category = (String) itemObject.get("category");
                String value = (String) itemObject.get("fcstValue");
                WeatherForecastAPI weatherForecastAPI = findExistingForecast(forcastDate, forcastTime);
                updateEntityFromJsonObject(weatherForecastAPI, category, value);
            }

            log.info("WeatherSht Update : {}", baseTime);

//            log.info("weatherForecastList size: " + weatherForecastList.size());
        } catch (NullPointerException e) {
            retryInputData();
        }
    }

    @SneakyThrows
    private void retryInputData() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::inputData, 1, TimeUnit.MINUTES);
    }


    // API를 클래스에 매핑
    public void updateEntityFromJsonObject(Object entity, String cate, String values) {

        for (Field field : entity.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            field.setAccessible(true);
            try {
                if (cate.equals(fieldName)) {
                    field.set(entity, values);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error("updateEntityFromJsonObject : ", e);
            }

        }
    }

    // API 데이터 불러오기
    private String SetUrl(String baseTimes, String url, LocalDateTime localDateTime) {


        String baseDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));


        try {
            String urlParams = "?serviceKey=" + URLEncoder.encode(serviceKey, "UTF-8")
                    + "&pageNo=1&numOfRows=1000&dataType=JSON&base_date=" + baseDate + "&base_time=" + baseTimes + "&nx=64&ny=123";

            return integratedService.getWeatherJson(urlParams, url);
        } catch (IOException e) {
            log.error("setUrl : Connection error");
            return "NoData SetUrl";
        }
    }

    // API 데이터 중복체크
    private WeatherForecastAPI findExistingForecast(String fcstDate, String fcstTime) {

        for (WeatherForecastAPI cast : weatherForecastList) {
            if (cast.getFcstDate().equals(fcstDate) && cast.getFcstTime().equals(fcstTime)) {
                return cast;
            }
        }

        WeatherForecastAPI forecast = new WeatherForecastAPI();
        forecast.setFcstDate(fcstDate);
        forecast.setFcstTime(fcstTime);
        weatherForecastList.add(forecast); // 새로운 객체를 weatherForecastList에 추가
        return forecast;
    }
}