package com.sky.service;

import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO totalTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();
}
