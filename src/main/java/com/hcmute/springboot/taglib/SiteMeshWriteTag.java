package com.hcmute.springboot.taglib;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class SiteMeshWriteTag extends SimpleTagSupport {

    private String property;

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print("<sitemesh:write property=\"" + property + "\"/>");
    }
}