Message TrackManagementTranslationTree Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.9A(AC=4)."
        C. "FWD M.9A(AC=5)."
        D. "FWD M.9A(AC=6)."
        E. "FWD M.9A(AC=7)."
        F. "FWD M.9A(AC=0)."
        G. "FWD M.9A(AC=1)."
        H. "FWD M.9A(AC=2)."
        I. "FWD M.9A(AC=3)."
        J. "FWD M.9A(AC=8)."
        K. "FWD M.9A(AC=9)."
        L. "FWD M.9A(AC=10)."
        M. "FWD M.9A(AC=11)."
        N. "FWD M.9A(AC=12)."
        O. "FWD M.9A(AC=13)."
        P. "FWD M.9A(AC=14)."
        Q. "FWD M.9A(AC=15)."
        R. "FWD M.9A(AC=16)."
        S. "FWD M.9A(AC=17)."
        T. "FWD M.9A(AC=18)."
        U. "FWD M.9A(AC=19)."
        V. "FWD M.9A(AC=20)."
        W. "FWD M.9A(AC=21)."
        X. "FWD M.9A(AC=22)."
        Y. "FWD M.9A(AC=23)."
        Z. "FWD M.9A(AC=24)."


}
CONDITION {
    Node1: "ACTION, TRACK MANAGEMENT = 5, 6, OR 7."
    Node2: "ACTION, TRACK MANAGEMENT = 0."
    Node3: "ACTION, TRACK MANAGEMENT = 1."
    Node4: "ACTION, TRACK MANAGEMENT = 2."
    Node5: "ACTION, TRACK MANAGEMENT = 3."
    Node6: "ACTION, TRACK MANAGEMENT = 4."
    Node7: "ACTION, TRACK MANAGEMENT = 8."
    Node8: "ACTION, TRACK MANAGEMENT = 9."
    Node9: "ACTION, TRACK MANAGEMENT = 10."
    Node10: "ACTION, TRACK MANAGEMENT = 11."
    Node11: "ACTION, TRACK MANAGEMENT = 12."
    Node12: "ACTION, TRACK MANAGEMENT = 13."
    Node13: "ACTION, TRACK MANAGEMENT = 14."
    Node14: "ACTION, TRACK MANAGEMENT = 15."
    Node15: "ACTION, TRACK MANAGEMENT = 16."
    Node16: "ACTION, TRACK MANAGEMENT = 17."
    Node17: "ACTION, TRACK MANAGEMENT = 18."
    Node18: "ACTION, TRACK MANAGEMENT = 19."
    Node19: "ACTION, TRACK MANAGEMENT = 20."
    Node20: "ACTION, TRACK MANAGEMENT = 21."
    Node21: "ACTION, TRACK MANAGEMENT = 22."
    Node22: "ACTION, TRACK MANAGEMENT = 23."
    Node23: "ACTION, TRACK MANAGEMENT = 24."
}
IF (Node1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (Node2) {
        EXECUTE(F)


}
ELSE {
    IF (Node3) {
        EXECUTE(G)


}
ELSE {
    IF (Node4) {
        EXECUTE(H)


}
ELSE {
    IF (Node5) {
        EXECUTE(I)


}
ELSE {
    IF (Node6) {
        EXECUTE(B)


}
ELSE {
    IF (Node7) {
        EXECUTE(J)


}
ELSE {
    IF (Node8) {
        EXECUTE(K)


}
ELSE {
    IF (Node9) {
        EXECUTE(L)


}
ELSE {
    IF (Node10) {
        EXECUTE(M)


}
ELSE {
    IF (Node11) {
        EXECUTE(N)


}
ELSE {
    IF (Node12) {
        EXECUTE(O)


}
ELSE {
    IF (Node13) {
        EXECUTE(P)


}
ELSE {
    IF (Node14) {
        EXECUTE(Q)


}
ELSE {
    IF (Node15) {
        EXECUTE(R)


}
ELSE {
    IF (Node16) {
        EXECUTE(S)


}
ELSE {
    IF (Node17) {
        EXECUTE(T)


}
ELSE {
    IF (Node18) {
        EXECUTE(U)


}
ELSE {
    IF (Node19) {
        EXECUTE(V)


}
ELSE {
    IF (Node20) {
        EXECUTE(W)


}
ELSE {
    IF (Node21) {
        EXECUTE(X)


}
ELSE {
    IF (Node22) {
        EXECUTE(Y)


}
ELSE {
    IF (Node23) {
        EXECUTE(Z)


}
ELSE {
    EXECUTE(A)
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
