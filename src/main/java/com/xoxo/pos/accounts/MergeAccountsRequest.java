package com.xoxo.pos.accounts; import java.util.List; public record MergeAccountsRequest(List<Long> accountIds,String customerName){}
