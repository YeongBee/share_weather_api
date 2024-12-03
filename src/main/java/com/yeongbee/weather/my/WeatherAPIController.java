package com.yeongbee.weather.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/weather")
@Slf4j
public class WeatherAPIController {

    private final WeatherShtService shtService;
    private final WeatherMidService midService;


    @GetMapping("")
    public String weatherShows(Model model) {

        List<WeatherSetEntity> weatherSht = shtService.getWeatherShtSet();


        weatherSht.addAll(midService.getWeatherMidSet());
        WeatherUSN weatherUSN = shtService.getWeatherUsn();
        List<WeatherForecastAPI> fiveWeather = shtService.getShtFiveWeather();

        model.addAttribute("weatherUSN", weatherUSN);       // 현재온도
        model.addAttribute("weatherFive", fiveWeather);     // 2시간 마다 데이터
        model.addAttribute("weatherSets", weatherSht);      // 4 ~ 7  일 데이터


        for (WeatherSetEntity weatherSetEntity : weatherSht) {
            log.warn("weatherSetEntity: {}", weatherSetEntity);
        }

//        log.info("weatherUSN={}",weatherUSN.toString());
//        log.info("weatherFive={}",fiveWeather.toString());
//        log.info("weatherSht={}",weatherSht.toString());
//        log.info("myApiEntityExternal={}",myApiEntityExternal.toString());
        return "weather/weather_view";
    }
}
