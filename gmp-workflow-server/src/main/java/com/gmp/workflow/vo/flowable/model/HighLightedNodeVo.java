package com.gmp.workflow.vo.flowable.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class HighLightedNodeVo implements Serializable {
    public HighLightedNodeVo() {}

    public HighLightedNodeVo(List<String> highLightedFlows, List<String> activeActivityIds) {
        this.highLightedFlows = highLightedFlows;
        this.activeActivityIds = activeActivityIds;
    }

    public HighLightedNodeVo(List<String> highLightedFlows, List<String> activeActivityIds, String modelXml, String modelName) {
        this.highLightedFlows = highLightedFlows;
        this.activeActivityIds = activeActivityIds;
        this.modelXml = modelXml;
        this.modelName = modelName;
    }

    private List<String> highLightedFlows;
    private List<String> activeActivityIds;
    private List<String> hisActiveActivityIds;
    private List<String> nullActiveActivityIds;
    private String modelXml;
    private String modelName;
}
