package net.heithoff

import net.heithoff.base.Email

class TestUtil {
    static Email quickNewEmail(Worker worker1) {
        Email newEmail = worker1.workEmail.clone()
        newEmail.wid = null
        newEmail.isPrimary = false
        newEmail.resetDirty()
        newEmail.address = Math.random() + newEmail.address
        newEmail
    }
}
