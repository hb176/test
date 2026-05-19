package com.gmp.workflow.tools.pager;

public enum ORDERBY {
    DESC, ASC;

    public ORDERBY reverse() {
        return (this == ASC) ? DESC : ASC;
    }
}
