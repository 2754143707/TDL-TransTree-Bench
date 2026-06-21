Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE"
        B. "FWD J7.0(ACT = 0) WITH APPROPRIATE ALERT DATA"
        C. "FWD J7.0"
        D. "RETAIN DATA IN FJU DATABASE"
        E. "FWD J7.1"
        F. "FWD J7.5"
        G. "FWD J10.5"
        H. "RETAIN CONTROLLING UNIT ADDRESS IN FJU DATABASE"
        I. "FWD J7.0 ON SURVEILLANCE NPG"
        Z. "END TRANSLATION"


}
CONDITION {
    C1: "ACTION CODE = 0"
    C2: "ACTION CODE = 1 OR 2"
    C3: "ACTION CODE = 3"
    C4: "TN IS BEING REPORTED ON LINK 16"
    C5: "STATUS INDICATOR = 1"
    C6: "CONFIDENCE INDICATOR = 0 AND FJU IS RETAINING ID DATA FOR SAME TN"
    C7: "ACTION CODE = 5 OR 7"
    C8: "ACTION CODE = 5"
    C9: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU"
}
IF (C1) {
    IF (C4) {
        IF (C9) {
            EXECUTE(E, F, Z)


    }
    ELSE {
        EXECUTE(A, F, Z)


}
}
ELSE {
    IF (C5) {
        IF (C9) {
            EXECUTE(A, C, D, Z)


    }
    ELSE {
        EXECUTE(A, C, D, G, Z)


}
}
ELSE {
    IF (C6) {
        EXECUTE(D, G, Z)


}
ELSE {
    EXECUTE(G, Z)
}
}
}
}
ELSE {
    IF (C2) {
        IF (C3) {
            IF (C5) {
                EXECUTE(C, D, G, Z)


        }
        ELSE {
            IF (C7) {
                IF (C8) {
                    EXECUTE(C, H, Z)


            }
            ELSE {
                EXECUTE(A, Z)


        }


}
ELSE {
    EXECUTE(A, B, I, Z)
}
}
}
ELSE {
    EXECUTE(A, B, I, Z)
}
}
ELSE {
    EXECUTE(A, B, I, Z)
}
}
}