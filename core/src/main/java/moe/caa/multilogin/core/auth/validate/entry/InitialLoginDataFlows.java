package moe.caa.multilogin.core.auth.validate.entry;

import lombok.SneakyThrows;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

public class InitialLoginDataFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public InitialLoginDataFlows(MultiCore core) {
        this.core = core;
    }

    @SneakyThrows
    @Override
    public Signal run(ValidateContext validateContext) {
        if (!core.getSqlManager().getUserDataTable().dataExists(
                validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
        )) {
            core.getSqlManager().getUserDataTable().insertNewData(
                    validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                    validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                    null
            );
        }
        return Signal.PASSED;
    }
}
