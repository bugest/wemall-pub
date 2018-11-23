package com.example.disconf.demo.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;


/**
 * 金融系数文件
 *
 **/
@Service
//@DisconfFile(filename = "coefficients.properties")
public class Coefficients {

    public static final String key = "discountRate";

    @Value(value = "2.0d")
    private Double discount;


    /**
     * 折扣率，分布式配置
     *
     * @return
     */
    @DisconfItem(key = key)
    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}