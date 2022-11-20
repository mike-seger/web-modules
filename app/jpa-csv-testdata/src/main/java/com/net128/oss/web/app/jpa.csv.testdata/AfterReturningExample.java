package com.net128.oss.web.app.jpa.csv.testdata;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class AfterReturningExample {
    @AfterReturning(
        pointcut="execution(* com.net128.oss.web.lib.jpa.csv.JpaCsvController.putCsv(..))",
        returning="retVal")
    public void putCsv(Object retVal) {

    }
}