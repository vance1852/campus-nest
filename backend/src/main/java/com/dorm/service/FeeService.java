package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BusinessException;
import com.dorm.common.PageResult;
import com.dorm.entity.Bed;
import com.dorm.entity.FeeRecord;
import com.dorm.entity.Student;
import com.dorm.mapper.BedMapper;
import com.dorm.mapper.FeeRecordMapper;
import com.dorm.mapper.StudentMapper;
import com.dorm.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRecordMapper feeRecordMapper;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;

    public PageResult<FeeRecord> page(int current, int size, Long studentId, String studentName, Integer feeType, Integer status, String billCycle) {
        Page<FeeRecord> page = new Page<>(current, size);
        return PageResult.of(feeRecordMapper.selectPageWithInfo(page, studentId, studentName, feeType, status, billCycle));
    }

    public PageResult<FeeRecord> myPage(int current, int size, Integer feeType, Integer status, String billCycle) {
        Student student = studentMapper.selectByUserId(UserContext.getUserId());
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        Page<FeeRecord> page = new Page<>(current, size);
        return PageResult.of(feeRecordMapper.selectPageWithInfo(page, student.getId(), null, feeType, status, billCycle));
    }

    public void create(FeeRecord feeRecord) {
        // 验证学生是否存在
        Student student = studentMapper.selectById(feeRecord.getStudentId());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        // 获取学生的房间ID
        Bed bed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, student.getId()));
        if (bed == null) {
            throw new BusinessException("学生未分配宿舍，无法创建账单");
        }
        feeRecord.setRoomId(bed.getRoomId());
        feeRecord.setStatus(0);
        feeRecordMapper.insert(feeRecord);
    }

    public void update(Long id, FeeRecord feeRecord) {
        FeeRecord existing = feeRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (existing.getStatus() == 1) {
            throw new BusinessException("已缴费的记录不能修改");
        }
        feeRecord.setId(id);
        feeRecordMapper.updateById(feeRecord);
    }

    public void delete(Long id) {
        FeeRecord existing = feeRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (existing.getStatus() == 1) {
            throw new BusinessException("已缴费的记录不能删除");
        }
        feeRecordMapper.deleteById(id);
    }

    public void confirmPayment(Long id, String remark) {
        FeeRecord feeRecord = feeRecordMapper.selectById(id);
        if (feeRecord == null) {
            throw new BusinessException("缴费记录不存在");
        }
        if (feeRecord.getStatus() == 1) {
            throw new BusinessException("该账单已缴费");
        }
        feeRecord.setStatus(1);
        feeRecord.setPayTime(LocalDateTime.now());
        feeRecord.setPayUserId(UserContext.getUserId());
        feeRecord.setRemark(remark);
        feeRecordMapper.updateById(feeRecord);
    }

    public Long getUnpaidCount() {
        return feeRecordMapper.selectCount(new LambdaQueryWrapper<FeeRecord>().eq(FeeRecord::getStatus, 0));
    }

    public Long getMyUnpaidCount() {
        Student student = studentMapper.selectByUserId(UserContext.getUserId());
        if (student == null) {
            return 0L;
        }
        return feeRecordMapper.selectCount(new LambdaQueryWrapper<FeeRecord>()
                .eq(FeeRecord::getStudentId, student.getId())
                .eq(FeeRecord::getStatus, 0));
    }
}
