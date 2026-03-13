package com.dorm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BusinessException;
import com.dorm.common.PageResult;
import com.dorm.entity.FeeRecord;
import com.dorm.entity.Student;
import com.dorm.entity.Bed;
import com.dorm.mapper.FeeRecordMapper;
import com.dorm.mapper.StudentMapper;
import com.dorm.mapper.BedMapper;
import com.dorm.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeeRecordService {

    private final FeeRecordMapper feeRecordMapper;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;

    public PageResult<FeeRecord> page(int current, int size, Integer status, Integer feeType, String billCycle) {
        Page<FeeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FeeRecord::getStatus, status);
        }
        if (feeType != null) {
            wrapper.eq(FeeRecord::getFeeType, feeType);
        }
        if (billCycle != null && !billCycle.isEmpty()) {
            wrapper.eq(FeeRecord::getBillCycle, billCycle);
        }
        wrapper.orderByDesc(FeeRecord::getCreateTime);
        return PageResult.of(feeRecordMapper.selectPage(page, wrapper));
    }

    public PageResult<FeeRecord> myPage(int current, int size, Integer status, Integer feeType, String billCycle) {
        Student student = studentMapper.selectByUserId(UserContext.getUserId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        Page<FeeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeRecord::getStudentId, student.getId());
        if (status != null) {
            wrapper.eq(FeeRecord::getStatus, status);
        }
        if (feeType != null) {
            wrapper.eq(FeeRecord::getFeeType, feeType);
        }
        if (billCycle != null && !billCycle.isEmpty()) {
            wrapper.eq(FeeRecord::getBillCycle, billCycle);
        }
        wrapper.orderByDesc(FeeRecord::getCreateTime);
        return PageResult.of(feeRecordMapper.selectPage(page, wrapper));
    }

    public void create(FeeRecord feeRecord) {
        // 验证学生是否存在
        Student student = studentMapper.selectById(feeRecord.getStudentId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        // 验证房间是否分配
        Bed bed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getStudentId, student.getId()));
        if (bed == null) {
            throw new BusinessException("该学生未分配宿舍");
        }
        feeRecord.setRoomId(bed.getRoomId());
        feeRecord.setStatus(0);
        feeRecord.setCreateTime(LocalDateTime.now());
        feeRecordMapper.insert(feeRecord);
    }

    public void update(FeeRecord feeRecord) {
        FeeRecord exist = feeRecordMapper.selectById(feeRecord.getId());
        if (exist == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (exist.getStatus() == 1) {
            throw new BusinessException("已缴费记录不可修改");
        }
        feeRecord.setUpdateTime(LocalDateTime.now());
        feeRecordMapper.updateById(feeRecord);
    }

    public void delete(Long id) {
        FeeRecord exist = feeRecordMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (exist.getStatus() == 1) {
            throw new BusinessException("已缴费记录不可删除");
        }
        feeRecordMapper.deleteById(id);
    }

    public void confirmPayment(Long id) {
        FeeRecord feeRecord = feeRecordMapper.selectById(id);
        if (feeRecord == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (feeRecord.getStatus() == 1) {
            throw new BusinessException("该账单已缴费");
        }
        feeRecord.setStatus(1);
        feeRecord.setPayTime(LocalDateTime.now());
        feeRecord.setUpdateTime(LocalDateTime.now());
        feeRecordMapper.updateById(feeRecord);
    }

    public long getUnpaidCount() {
        return feeRecordMapper.selectCount(new LambdaQueryWrapper<FeeRecord>()
                .eq(FeeRecord::getStatus, 0));
    }
}
