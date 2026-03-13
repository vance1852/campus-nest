package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_record")
public class FeeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long roomId;
    private Integer feeType;
    private BigDecimal amount;
    private String billCycle;
    private Integer status;
    private LocalDateTime payTime;
    private Long payUserId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
    
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String studentNo;
    @TableField(exist = false)
    private String roomInfo;
    @TableField(exist = false)
    private String payUserName;
}
