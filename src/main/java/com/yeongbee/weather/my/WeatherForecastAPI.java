package com.yeongbee.weather.my;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class WeatherForecastAPI {

    private String fcstDate;    // 예상날자
    private String fcstTime;    // 예상 시간
    private String POP;     // 강수확률
    private String PTY;     // 강수형태
    private String REH;     // 습도
    private String SNO;     // 1시간 신적설
    private String SKY;     // 하늘 상태
    private String TMP;     // 1시간 기온
    private String TMN;     // 일 최저기온
    private String TMX;     // 일 최고기온
    private String VEC;     // 풍향
    private String WSD;     //풍속
}
