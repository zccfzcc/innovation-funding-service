package com.worth.ifs.validator;

import com.worth.ifs.finance.resource.cost.OtherFunding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class validates the FormInputResponse, it checks if the maximum word count has been exceeded.
 */
@Component
public class OtherFundingValidator implements Validator {

    private static final Log LOG = LogFactory.getLog(OtherFundingValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return OtherFunding.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OtherFunding otherFunding = (OtherFunding) target;
        String fundingSource = otherFunding.getFundingSource();
        if(fundingSource != null && !fundingSource.equals("Other Funding")){
            String securedDate = otherFunding.getSecuredDate();
            if(!isValidDate(securedDate)) {
                errors.reject("InvalidDate", "Invalid secured date.  Please use MM-YYYY format.");
            }
        }
    }

    private boolean isValidDate(final String input){
        SimpleDateFormat format = new SimpleDateFormat("MM-yyyy");
        try {
            format.parse(input);
            return true;
        } catch(ParseException e){
            return false;
        }
    }
}
