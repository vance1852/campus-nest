package com.dorm.controller;

import com.dorm.annotation.OperationLog;
import com.dorm.annotation.RequireRole;
import com.dorm.common.PageResult;
import com.dorm.common.Result;
import com.dorm.common.RoleConstants;
import com.dorm.entity.FeeRecord;
import com.dorm.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @GetMapping
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    public Result<PageResult<FeeRecord>> page(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Long studentId,
                                              @RequestParam(required = false) String studentName,
                                              @RequestParam(required = false) Integer feeType,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String billCycle) {
        return Result.success(feeService.page(current, size, studentId, studentName, feeType, status, billCycle));
    }

    @GetMapping("/my")
    @RequireRole(RoleConstants.STUDENT)
    public Result<PageResult<FeeRecord>> myPage(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Integer feeType,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) String billCycle) {
        return Result.success(feeService.myPage(current, size, feeType, status, billCycle));
    }

    @PostMapping
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    @OperationLog(module = "缴费管理", operation = "创建缴费账单")
    public Result<Void> create(@RequestBody FeeRecord feeRecord) {
        feeService.create(feeRecord);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    @OperationLog(module = "缴费管理", operation = "修改缴费账单")
    public Result<Void> update(@PathVariable Long id, @RequestBody FeeRecord feeRecord) {
        feeService.update(id, feeRecord);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    @OperationLog(module = "缴费管理", operation = "删除缴费账单")
    public Result<Void> delete(@PathVariable Long id) {
        feeService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    @OperationLog(module = "缴费管理", operation = "确认缴费")
    public Result<Void> confirmPayment(@PathVariable Long id,
                                       @RequestParam(required = false) String remark) {
        feeService.confirmPayment(id, remark);
        return Result.success();
    }

    @GetMapping("/unpaid/count")
    @RequireRole(RoleConstants.ADMIN_AND_MANAGER)
    public Result<Long> getUnpaidCount() {
        return Result.success(feeService.getUnpaidCount());
    }

    @GetMapping("/my/unpaid/count")
    @RequireRole(RoleConstants.STUDENT)
    public Result<Long> getMyUnpaidCount() {
        return Result.success(feeService.getMyUnpaidCount());
    }
}
