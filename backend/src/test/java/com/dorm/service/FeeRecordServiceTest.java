package com.dorm.service;

import com.dorm.common.BusinessException;
import com.dorm.entity.FeeRecord;
import com.dorm.mapper.FeeRecordMapper;
import com.dorm.mapper.StudentMapper;
import com.dorm.mapper.BedMapper;
import com.dorm.entity.Student;
import com.dorm.entity.Bed;
import com.dorm.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeeRecordServiceTest {

    @Mock
    private FeeRecordMapper feeRecordMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private BedMapper bedMapper;

    @InjectMocks
    private FeeRecordService feeRecordService;

    private FeeRecord feeRecord;
    private Student student;
    private Bed bed;

    @BeforeEach
    void setUp() {
        feeRecord = new FeeRecord();
        feeRecord.setId(1L);
        feeRecord.setStudentId(1L);
        feeRecord.setRoomId(1L);
        feeRecord.setFeeType(1);
        feeRecord.setAmount(new BigDecimal("100.00"));
        feeRecord.setBillCycle("2024-01");
        feeRecord.setStatus(0);
        feeRecord.setCreateTime(LocalDateTime.now());

        student = new Student();
        student.setId(1L);
        student.setName("张三");
        student.setStudentNo("2024001");

        bed = new Bed();
        bed.setId(1L);
        bed.setRoomId(1L);
        bed.setStudentId(1L);
    }

    @Test
    void testCreateFeeRecord_Success() {
        when(studentMapper.selectById(1L)).thenReturn(student);
        when(bedMapper.selectOne(any())).thenReturn(bed);
        when(feeRecordMapper.insert(any(FeeRecord.class))).thenReturn(1);

        assertDoesNotThrow(() -> feeRecordService.create(feeRecord));

        verify(studentMapper, times(1)).selectById(1L);
        verify(bedMapper, times(1)).selectOne(any());
        verify(feeRecordMapper, times(1)).insert(any(FeeRecord.class));
    }

    @Test
    void testCreateFeeRecord_StudentNotFound() {
        when(studentMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.create(feeRecord));

        assertEquals("学生信息不存在", exception.getMessage());
        verify(studentMapper, times(1)).selectById(1L);
        verify(bedMapper, never()).selectOne(any());
        verify(feeRecordMapper, never()).insert(any(FeeRecord.class));
    }

    @Test
    void testCreateFeeRecord_StudentNotAssignedRoom() {
        when(studentMapper.selectById(1L)).thenReturn(student);
        when(bedMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.create(feeRecord));

        assertEquals("该学生未分配宿舍", exception.getMessage());
        verify(studentMapper, times(1)).selectById(1L);
        verify(bedMapper, times(1)).selectOne(any());
        verify(feeRecordMapper, never()).insert(any(FeeRecord.class));
    }

    @Test
    void testConfirmPayment_Success() {
        when(feeRecordMapper.selectById(1L)).thenReturn(feeRecord);
        when(feeRecordMapper.updateById(any(FeeRecord.class))).thenReturn(1);

        assertDoesNotThrow(() -> feeRecordService.confirmPayment(1L));

        assertEquals(1, feeRecord.getStatus());
        assertNotNull(feeRecord.getPayTime());
        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, times(1)).updateById(any(FeeRecord.class));
    }

    @Test
    void testConfirmPayment_RecordNotFound() {
        when(feeRecordMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.confirmPayment(1L));

        assertEquals("缴费记录不存在", exception.getMessage());
        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, never()).updateById(any(FeeRecord.class));
    }

    @Test
    void testConfirmPayment_AlreadyPaid() {
        feeRecord.setStatus(1);
        when(feeRecordMapper.selectById(1L)).thenReturn(feeRecord);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.confirmPayment(1L));

        assertEquals("该账单已缴费", exception.getMessage());
        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, never()).updateById(any(FeeRecord.class));
    }

    @Test
    void testDeleteFeeRecord_Success() {
        when(feeRecordMapper.selectById(1L)).thenReturn(feeRecord);
        when(feeRecordMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> feeRecordService.delete(1L));

        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteFeeRecord_RecordNotFound() {
        when(feeRecordMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.delete(1L));

        assertEquals("缴费记录不存在", exception.getMessage());
        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteFeeRecord_AlreadyPaid() {
        feeRecord.setStatus(1);
        when(feeRecordMapper.selectById(1L)).thenReturn(feeRecord);

        BusinessException exception = assertThrows(BusinessException.class, () -> feeRecordService.delete(1L));

        assertEquals("已缴费记录不可删除", exception.getMessage());
        verify(feeRecordMapper, times(1)).selectById(1L);
        verify(feeRecordMapper, never()).deleteById(anyLong());
    }

    @Test
    void testGetUnpaidCount() {
        when(feeRecordMapper.selectCount(any())).thenReturn(5L);

        long count = feeRecordService.getUnpaidCount();

        assertEquals(5L, count);
        verify(feeRecordMapper, times(1)).selectCount(any());
    }
}
