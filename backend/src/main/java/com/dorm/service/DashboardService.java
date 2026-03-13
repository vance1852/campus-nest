package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import com.dorm.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final StudentMapper studentMapper;
    private final RepairRequestMapper repairMapper;
    private final VisitorRecordMapper visitorMapper;
    private final FeeRecordMapper feeRecordMapper;

    public Map<String, Object> getStats() {
        Integer role = UserContext.getRole();
        Map<String, Object> stats = new HashMap<>();
        
        if (role == 3) {
            // 学生角色：显示个人相关数据
            Long userId = UserContext.getUserId();
            Student student = studentMapper.selectByUserId(userId);
            if (student != null) {
                stats.put("studentId", student.getId());
                stats.put("studentName", student.getName());
                stats.put("studentNo", student.getStudentNo());
                // 获取住宿信息
                Bed bed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, student.getId()));
                if (bed != null) {
                    Room room = roomMapper.selectById(bed.getRoomId());
                    if (room != null) {
                        Building building = buildingMapper.selectById(room.getBuildingId());
                        stats.put("dormInfo", (building != null ? building.getName() : "") + " " + room.getRoomNumber() + "室 " + bed.getBedNumber() + "床");
                        stats.put("roomId", room.getId());
                    }
                } else {
                    stats.put("dormInfo", "未分配宿舍");
                }
                // 个人维修申请统计
                stats.put("myRepairTotal", repairMapper.selectCount(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, student.getId())));
                stats.put("myRepairPending", repairMapper.selectCount(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, student.getId()).eq(RepairRequest::getStatus, 0)));
                stats.put("myRepairProcessing", repairMapper.selectCount(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, student.getId()).eq(RepairRequest::getStatus, 1)));
                stats.put("myRepairCompleted", repairMapper.selectCount(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, student.getId()).eq(RepairRequest::getStatus, 2)));
                // 个人未缴费统计
                stats.put("myUnpaidFees", feeRecordMapper.selectCount(new LambdaQueryWrapper<FeeRecord>().eq(FeeRecord::getStudentId, student.getId()).eq(FeeRecord::getStatus, 0)));
            }
        } else {
            // 管理员/宿管角色：显示全局统计数据
            stats.put("buildingCount", buildingMapper.selectCount(null));
            stats.put("roomCount", roomMapper.selectCount(null));
            stats.put("totalBeds", bedMapper.selectCount(null));
            stats.put("occupiedBeds", bedMapper.selectCount(new LambdaQueryWrapper<Bed>().eq(Bed::getStatus, 1)));
            stats.put("studentCount", studentMapper.selectCount(null));
            stats.put("pendingRepairs", repairMapper.selectCount(new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStatus, 0)));
            stats.put("visitingCount", visitorMapper.selectCount(new LambdaQueryWrapper<VisitorRecord>().eq(VisitorRecord::getStatus, 0)));
            stats.put("unpaidFees", feeRecordMapper.selectCount(new LambdaQueryWrapper<FeeRecord>().eq(FeeRecord::getStatus, 0)));
        }
        return stats;
    }
}
