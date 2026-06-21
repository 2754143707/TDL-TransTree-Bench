Message M.14 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD J13.2, J13.3, J13.4, OR J13.5 MESSAGE.
        C. FWD J10.2 MESSAGE.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: K/ES = 0, 1, OR 12.
        Condition2: W/ES 2, 5, 6, 8, OR 10 AND WEAPON TYPE = 0-10.
        Condition3: W/ES = 3 AND WEAPON TYPE = 0-2, 4, 6-8, OR 10.
        Condition4: W/ES = 4 OR 7 AND WEAPON TYPE = 1-10.
        Condition5: W/ES = 9 AND WEAPON TYPE = 0.
    }

    LogicBlock {
        IF (Condition1) {
            EXECUTE(B, Z)
        }
        ELSE {
            IF (Condition2) {
                EXECUTE(C, Z)
            }
            ELSE {
                IF (Condition3) {
                    EXECUTE(C, Z)
                }
                ELSE {
                    IF (Condition4) {
                        EXECUTE(C, Z)
                    }
                    ELSE {
                        IF (Condition5) {
                            EXECUTE(C, Z)
                        }
                        ELSE {
                            EXECUTE(A, Z)
                        }
                    }
                }
            }
        }
    }
}