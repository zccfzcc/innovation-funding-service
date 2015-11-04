package com.worth.ifs.user.domain;

import com.worth.ifs.BaseBuilder;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by dwatson on 08/10/15.
 */
public class RoleBuilder extends BaseBuilder<Role, RoleBuilder> {

    private RoleBuilder() {
        super();
    }

    private RoleBuilder(List<BiConsumer<Integer, Role>> multiActions) {
        super(multiActions);
    }

    public static RoleBuilder newRole() {
        return new RoleBuilder();
    }

    @Override
    protected RoleBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Role>> actions) {
        return new RoleBuilder(actions);
    }

    @Override
    protected Role createInitial() {
        return new Role();
    }

    public RoleBuilder withType(UserRoleType... types) {
        return with((type, role) -> role.setName(type.getName()), types);
    }
}
