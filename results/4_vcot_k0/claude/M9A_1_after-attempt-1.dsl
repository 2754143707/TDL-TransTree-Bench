Message M9A Translation Trees {
    ACTION {
        A. DISCARD MESSAGE.
        B. FWD J7.0.
        C. RETAIN DATA IN FJU DATABASE.
        D. FWD J7.0.
        E. FWD J7.1.
        F. FWD J10.5.
        G. FWD J7.5.
        H. FWD J7.0.
        I. FWD J7.0.
        Z. END TRANSLATION.

}
CONDITION {
    N1: PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU.
    N2: ACTION CODE = 0.
    N3: ACTION CODE = 5 OR 7.
    N4: STATUS INDICATOR = 1.
    N5: TN = 0.
    N6: ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7.
    N7: ACTION CODE = 6.
    N8: ACTION CODE = 1.
    N9: IFF/SIF ACTION CODE = 0.
}
IF (N1) {
    IF (N4) {
        EXECUTE(E, F, Z)

}
ELSE {
    IF (N5) {
        IF (N9) {
            EXECUTE(A, C, D, Z)

    }
    ELSE {
        EXECUTE(A, F, Z)

}
}
ELSE {
    IF (N6) {
        IF (N9) {
            EXECUTE(A, C, D, G, Z)

    }
    ELSE {
        EXECUTE(D, G, Z)

}
}
ELSE {
    EXECUTE(D, G, Z)
}
}
}
}
ELSE {
    IF (N2) {
        IF (N5) {
            EXECUTE(C, D, G, Z)

    }
    ELSE {
        IF (N7) {
            EXECUTE(A, B, I)

    }
    ELSE {
        IF (N8) {
            IF (N9) {
                EXECUTE(C, H, Z)

        }
        ELSE {
            EXECUTE(A, Z)

    }
}
ELSE {
    EXECUTE(A, B, I)
}
}
}
}
ELSE {
    IF (N3) {
        IF (N5) {
            EXECUTE(C, D, G, Z)

    }
    ELSE {
        IF (N7) {
            EXECUTE(A, B, I)

    }
    ELSE {
        IF (N8) {
            IF (N9) {
                EXECUTE(C, H, Z)

        }
        ELSE {
            EXECUTE(A, Z)

    }
}
ELSE {
    EXECUTE(A, B, I)
}
}
}
}
ELSE {
    EXECUTE(A, B, I)
}
}
}
}
