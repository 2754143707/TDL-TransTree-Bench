Message M6D_Translation_Tree Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J14.2I/J14.2E0/J14.2C3."
        C. "FWD J14.2I/J14.2E0."
        D. "FWD J14.2I."
        E. "SET INDICATOR TO TRANSMIT M.6D CANTCO."
        F. "FWD J14.2I/J14.2E0/J14.2C4."
        K. "GO TO TEST NODE 10."
        Z. "END TRANSLATION."
        G. "FWD J14.2I/J14.2E0/J14.2C1."
        H. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3."
        I. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3/J14.2C4."
        J. "FWD J14.2I/J142E0/J14.2C1/J14.2C4."


}
CONDITION {
    Node1: "CONTROL = 8."
    Node2: "CONTROL = 9 OR 10."
    Node3: "CONTROL = 11."
    Node4: "ADDRESSEE IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU."
    Node5: "ADDRESSEE IS INACTIVE."
    Node6: "R/C = 0."
    Node7: "R/C = 2."
    Node8: "CONTROL = 0, 1, 2, OR 3."
    Node9: "CONTROL = 4."
    Node10: "CONTROL = 5."
    Node11: "FREQUENCY/FREQUENCY RANGE = 0."
    Node12: "EMITTER FUNCTION AND EMITTER NUMBER = 0."
    Node13: "CONTROL = 7, 12, OR 13 AND REFERENCED/CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13."
    Node14: "CONTROL = 14 OR 15."
}
IF (Node1) {
    EXECUTE(A)
}
ELSE {
    IF (Node2) {
        EXECUTE(A)


}
ELSE {
    IF (Node3) {
        EXECUTE(A)


}
ELSE {
    IF (Node4) {
        IF (Node5) {
            EXECUTE(A)


    }
    ELSE {
        IF (Node6) {
            EXECUTE(B)


    }
    ELSE {
        IF (Node7) {
            EXECUTE(C)


    }
    ELSE {
        IF (Node8) {
            EXECUTE(D)


    }
    ELSE {
        IF (Node9) {
            EXECUTE(E)


    }
    ELSE {
        IF (Node10) {
            IF (Node11) {
                EXECUTE(F)


        }
        ELSE {
            IF (Node12) {
                EXECUTE(G)


        }
        ELSE {
            IF (Node13) {
                EXECUTE(H)


        }
        ELSE {
            IF (Node14) {
                EXECUTE(I)


        }
        ELSE {
            EXECUTE(J)


    }
}
}
}
}
ELSE {
    EXECUTE(K)
}
}
}
}
}
}
}
ELSE {
    EXECUTE(A)
}
}
}
}
}
