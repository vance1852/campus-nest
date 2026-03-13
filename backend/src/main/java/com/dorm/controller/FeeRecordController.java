package com.dorm.controller;

import com.dorm.annotation.OperationLog;
import com.dorm.annotation.RequireRole;
import com.dorm.common.PageResult;
import com.dorm.common.Result;
import com.dorm.common.RoleConstants;
import com.dorm.entity.FeeRecord;
import com.dorm.service.FeeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeRecordController {

    private final FeeRecordService feeRecordService;

    @GetMapping
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER})
    public Result<PageResult<FeeRecord>> page(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer feeType,
                                              @RequestParam(required = false) String billCycle) {
        return Result.success(feeRecordService.page(current, size, status, feeType, billCycle));
    }

    @GetMapping("/my")
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.STUDENT})
    public Result<PageResult<FeeRecord>> myPage(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer feeType,
                                                @RequestParam(required = false) String billCycle) {
        return Result.success(feeRecordService.myPage(current, size, status, feeType, billCycle));
    }

    @PostMapping
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER})
    @OperationLog(module = "缴费管理", operation = "新增缴费记录")
    public Result<Void> create(@RequestBody FeeRecord feeRecord) {
        feeRecordService.create(feeRecord);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER})
    @OperationLog(module = "缴费管理", operation = "修改缴费记录")
    public Result<Void> update(@PathVariable Long id, @RequestBody FeeRecord feeRecord) {
        feeRecord.setId(id);
        feeRecordService.update(feeRecord);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER})
    @OperationLog(module = "缴费管理", operation = "删除缴费记录")
    public Result<Void> delete(@PathVariable Long id) {
        feeRecordService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @RequireRole({RoleConstants.ADMIN, RoleConstants.DORM_MANAGER})
    @OperationLog(module = "缴费管理", operation = "确认缴费")
    public Result<Void> confirmPayment(@PathVariable Long id) {
        feeRecordService.confirmPayment(id);
        return Result.success();
    }
}
