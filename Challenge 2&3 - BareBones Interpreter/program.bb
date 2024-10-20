clear a;
clear b;
clear c;
clear d;

incr a;
incr b;
incr b;

sub addThree;
    incr c;
    incr c;
    incr c;
end;

sub addFive;
    incr a;
    incr a;
    incr a;
    incr a;
    incr a;
end;

call addThree;
call addFive;

if b not 0 do;
    incr d;
else;
    incr c;
end;

sub nestedCondition;
    if d not 0 do;
        incr a;
        if a not 0 do;
            decr b;
        else;
            incr c;
        end;
    else;
        incr d;
    end;
end;

call nestedCondition;

while b not 0 do;
    decr b;
    incr a;
    call addThree;
end;

sub nestedLoops;
    while d not 0 do;
        decr d;
        incr a;
        while a not 0 do;
            decr a;
            incr b;
        end;
    end;
end;

call nestedLoops;

