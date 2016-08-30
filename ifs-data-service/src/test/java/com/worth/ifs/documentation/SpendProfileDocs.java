package com.worth.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class SpendProfileDocs {

    public static final FieldDescriptor[] spendProfileTableFields = {
            fieldWithPath("eligibleCostPerCategoryMap").description("Map which holds the total eligible cost per category on the Spend Profile page"),
            fieldWithPath("monthlyCostsPerCategoryMap").description("Map which holds costs per month per category on the Spend Profile page"),
            fieldWithPath("months").description("List of months covered in the Spend Profile")
    };

    public static final FieldDescriptor[] spendProfileResourceFields = {
            fieldWithPath("id").description("Id of the Spend Profile Resource"),
            fieldWithPath("organisation").description("Organisation Id of the Spend Profile"),
            fieldWithPath("project").description("Project Id of the Spend Profile"),
            fieldWithPath("costCategoryType").description("Cost Category Type Id of the Spend Profile"),
            fieldWithPath("eligibleCosts").description("Eligible costs for each category of the Spend Profile"),
            fieldWithPath("spendProfileFigures").description("Spend Profile Figures for each month, for each category of the Spend Profile"),

    };
}
