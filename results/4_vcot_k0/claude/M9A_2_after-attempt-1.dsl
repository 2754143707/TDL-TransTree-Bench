Message M9A_Sheet3 Translation Trees {
    ACTION {
        J. FWD J7.0 WITH ACT=0
        K. FWD J7.0 WITH ACT=1
        L. FWD J7.1
        M. FWD J7.5
        N. RETAIN DATA IN FJU DATABASE
        O. FWD J7.0 WITH ACT=2
        P. FWD J7.0 WITH ACT=3
        Q. FWD J7.0 WITH ACT=4
        R. FWD J10.5
        S. FWD J7.0 WITH ACT=5
        T. FWD J7.0 WITH ACT=7
        U. FWD J7.5
        Z. END TRANSLATION


}
CONDITION {
    C10. ACTION = 0
    C11. ACTION = 1
    C12. ACTION = 2
    C13. ACTION = 3
    C14. ACTION = 4
    C15. ACTION = 5
    C16. ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7
    C17. IFF/SIF ACTION CODE = 0
    C18. PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU
    C19. STATUS INDICATOR = 1
    C20. TN = 0
    C21. ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7
    C22. IFF/SIF ACTION CODE = 0
}
IF (C10) {
    EXECUTE(J, N, Z)
}
ELSE {
    IF (C11) {
        EXECUTE(K, N, Z)


}
ELSE {
    IF (C12) {
        EXECUTE(N, Z)


}
ELSE {
    IF (C13) {
        IF (C18) {
            IF (C19) {
                IF (C20) {
                    EXECUTE(A, Z)


            }
            ELSE {
                EXECUTE(A, M, N, Z)


        }


}
ELSE {
    EXECUTE(L, Z)
}
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (C14) {
        EXECUTE(O, Z)


}
ELSE {
    IF (C15) {
        IF (C16) {
            IF (C17) {
                EXECUTE(S, U, Z)


        }
        ELSE {
            EXECUTE(S, Z)


    }
}
ELSE {
    EXECUTE(P, Z)
}
}
ELSE {
    EXECUTE(Q, Z)
}
}
}
}
}
}
}
