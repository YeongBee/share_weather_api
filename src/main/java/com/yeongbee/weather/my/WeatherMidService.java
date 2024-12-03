package com.yeongbee.weather.my;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
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


@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherMidService {

    private final WeatherApiIntegratedService integratedService;

    private final WeatherMidTmpApiEntity weatherMidTmpApiEntity = new WeatherMidTmpApiEntity();
    private final WeatherMidLandApiEntity weatherMidLandApiEntity = new WeatherMidLandApiEntity();

    @Value("${weather.mid.api}")
    private String serviceKey;

    @Value("${weather.mid.tmp.area}")
    private String tmpArea;

    @Value("${weather.mid.land.area}")
    private String landArea;

    @Value("${weather.mid.land.url}")
    private String landApiUrl;

    @Value("${weather.mid.tmp.url}")
    private String tmpApiUrl;

    @PostConstruct
    public void init() throws IOException, ParseException, IllegalAccessException {
        inputData();
    }


    public List<WeatherSetEntity> getWeatherMidSet(){
        List<WeatherSetEntity> weatherList = new ArrayList<>();

        // TODO
        // 중기예보 4일부터 10일까지 나옴 -> 단기예보 늘어남
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(6, 0);
        LocalTime endTime = LocalTime.of(18, 0);


        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
            weatherList.add(new WeatherSetEntity(weatherMidLandApiEntity.getRnSt4Am(),
                    weatherMidLandApiEntity.getRnSt4Pm(), weatherMidTmpApiEntity.getTaMin4(),
                    weatherMidTmpApiEntity.getTaMax4(), weatherMidLandApiEntity.getWf4Am(),
                    weatherMidLandApiEntity.getWf4Pm()));
        }

        weatherList.add(new WeatherSetEntity(weatherMidLandApiEntity.getRnSt5Am(),
                weatherMidLandApiEntity.getRnSt5Pm(), weatherMidTmpApiEntity.getTaMin5(),
                weatherMidTmpApiEntity.getTaMax5(), weatherMidLandApiEntity.getWf5Am(),
                weatherMidLandApiEntity.getWf5Pm()));

        weatherList.add(new WeatherSetEntity(weatherMidLandApiEntity.getRnSt6Am(),
                weatherMidLandApiEntity.getRnSt6Pm(), weatherMidTmpApiEntity.getTaMin6(),
                weatherMidTmpApiEntity.getTaMax6(), weatherMidLandApiEntity.getWf6Am(),
                weatherMidLandApiEntity.getWf6Pm()));
        weatherList.add(new WeatherSetEntity(weatherMidLandApiEntity.getRnSt7Am(),
                weatherMidLandApiEntity.getRnSt7Pm(), weatherMidTmpApiEntity.getTaMin7(),
                weatherMidTmpApiEntity.getTaMax7(), weatherMidLandApiEntity.getWf7Am(),
                weatherMidLandApiEntity.getWf7Pm()));

        return weatherList ;
    }


    @Scheduled(cron = "30 0 6,18 * * ?")
    private void inputData() throws IOException, IllegalAccessException, ParseException {

        LocalDateTime now = LocalDateTime.now();
        String currTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        int baseTimes = Integer.parseInt(now.format(DateTimeFormatter.ofPattern("HHmm")).substring(0, 2));

        String baseTime = (baseTimes >  6) && (baseTimes < 18) ? "06시" : "18시";

        log.info("MidWeather BaseDate={} BaseTime={}", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")), baseTime);


        String tmpUrl = setUrl(tmpApiUrl, tmpArea);
        String landUr = setUrl(landApiUrl, landArea);
        JSONObject tmpItemObject = (JSONObject) integratedService.getWeatherJsonArray(tmpUrl).get(0);
        JSONObject landItemObject = (JSONObject)integratedService.getWeatherJsonArray(landUr).get(0);

        updateEntityFromJsonObject(weatherMidTmpApiEntity, tmpItemObject);
        updateEntityFromJsonObject(weatherMidLandApiEntity, landItemObject);

        log.info("WeatherMidService Update DateTime = {}", currTime);
    }


    private void updateEntityFromJsonObject(Object entity, JSONObject itemObject) throws IllegalAccessException {

        for (Field field : entity.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            field.setAccessible(true);
            if (itemObject.containsKey(fieldName)) {
                field.set(entity, itemObject.get(fieldName));
            }
        }
    }


    // url를 판별하여 tmp, land의 데이터를 가져오고 area를 통해 지역 코드를 가져온다.
    // return 값은 받아온 json을 String로 반환
    private String setUrl(String urlKey, String area) throws IOException {

        LocalDateTime localDateTime = LocalDateTime.now();

        String baseDate;
        String baseTime;

        int baseTimes = Integer.parseInt(localDateTime.format(DateTimeFormatter.ofPattern("HHmm")).substring(0, 2));

        if (baseTimes >= 0 && baseTimes < 6) {
            // 현재 시간이 06시 미만인 경우, 전날 18시 데이터 가져오기
            localDateTime = localDateTime.minusDays(1);
            baseDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "1800";
        } else {
            baseDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = baseTimes >= 6 && baseTimes < 18 ? "0600" : "1800";
        }

        // 06, 18시에만 업데이트가 발생함
        String urlParams = "?serviceKey=" + URLEncoder.encode(serviceKey, "UTF-8")
                + "&pageNo=1&numOfRows=1000&dataType=JSON&regId="+area+"&tmFc="+baseDate+baseTime;

      return integratedService.getWeatherJson(urlParams, urlKey);
    }


}
