package com.dorm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.entity.FeeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FeeRecordMapper extends BaseMapper<FeeRecord> {
    
    @Select("<script>" +
            "SELECT fr.*, s.name as student_name, s.student_no, " +
            "CONCAT(b.name, ' ', r.room_number, '室') as room_info, " +
            "u.real_name as pay_user_name " +
            "FROM fee_record fr " +
            "LEFT JOIN student s ON fr.student_id = s.id " +
            "LEFT JOIN room r ON fr.room_id = r.id " +
            "LEFT JOIN building b ON r.building_id = b.id " +
            "LEFT JOIN sys_user u ON fr.pay_user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='studentId != null'> AND fr.student_id = #{studentId}</if>" +
            "<if test='studentName != null and studentName != \"\"'> AND s.name LIKE CONCAT('%', #{studentName}, '%')</if>" +
            "<if test='feeType != null'> AND fr.fee_type = #{feeType}</if>" +
            "<if test='status != null'> AND fr.status = #{status}</if>" +
            "<if test='billCycle != null and billCycle != \"\"'> AND fr.bill_cycle = #{billCycle}</if>" +
            "ORDER BY fr.create_time DESC" +
            "</script>")
    IPage<FeeRecord> selectPageWithInfo(Page<FeeRecord> page, 
                                        @Param("studentId") Long studentId,
                                        @Param("studentName") String studentName,
                                        @Param("feeType") Integer feeType,
                                        @Param("status") Integer status,
                                        @Param("billCycle") String billCycle);
}
