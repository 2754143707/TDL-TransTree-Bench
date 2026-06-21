Message M.4D Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD J5.4 MESSAGE.
        Z. END TRANSLATION.
    }

    CONDITION {
        Condition1: RECEIVED M.4D IS FOLLOWED BY AN M.84D MESSAGE AND RECEIVED M. 4D/M.84D MESSAGE SEQUENCE IS LEGAL.
        Condition2: RECEIVED M.4D/M.84D IS FOLLOWED BY AN M.4B MESSAGE.
        Condition3: REPORT TYPE = 1.
        Condition4: M.84D(SW=0) BEARING INDICATOR = 1 OR M.84D(SW=1) BEARING INDICATOR = 1, OR NO M.84D(SW=1) RECEIVED.
    }

    LogicBlock {
        IF (Condition1) {
            IF (Condition2) {
                IF (Condition3) {
                    IF (Condition4) {
                        EXECUTE(A, Z)
                    }
                    ELSE {
                        EXECUTE(B, Z)
                    }
                }
                ELSE {
                    EXECUTE(B, Z)
                }
            }
            ELSE {
                IF (Condition3) {
                    IF (Condition4) {
                        EXECUTE(A, Z)
                    }
                    ELSE {
                        EXECUTE(B, Z)
                    }
                }
                ELSE {
                    EXECUTE(B, Z)
                }
            }
        }
        ELSE {
            EXECUTE(A, Z)
        }
    }
}