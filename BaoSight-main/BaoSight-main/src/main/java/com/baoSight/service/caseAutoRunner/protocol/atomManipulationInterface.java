package com.baoSight.service.caseAutoRunner.protocol;

public interface atomManipulationInterface {
    // 初始化
    public void initialization();
    // 载入变量表
    public void loadingVriables();
    // 载入程序
    public void loadingPrograms(String caseName);
    // 读负载
    public void readCPUAndLoad();
    // 在线-下载（后处理）
    public void downloadToPLC();
    // 仿真下载
    public void simAndDownload();

}
