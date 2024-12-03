package com.yeongbee.weather.my;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class WeatherUSN {

    private String T1H;
    private String REH;
    private String PTY;
    private LocalDateTime localDateTime;


}
