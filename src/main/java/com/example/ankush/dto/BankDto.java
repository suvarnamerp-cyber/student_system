package com.example.ankush.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {
    private String bankName;
    private String branchName;
    private String accountNo;
    private String ifscCode;
}
