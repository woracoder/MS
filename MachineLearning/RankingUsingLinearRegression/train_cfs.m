function [Wml, Erms] = train_cfs(PhiTraining, TargetTraining, PhiValidation, TargetValidation, M, lamda)

    %http://www.mathworks.com/help/matlab/ref/inv.html
    %Matlab suggested: Replace inv(A)*b with A\b
    %Never use the inverse of a matrix to solve a linear system Ax=b with x=inv(A)*b, because it is slow and inaccurate.
    Wml = (((lamda * eye(M)) + (transpose(PhiTraining) * PhiTraining)) \ transpose(PhiTraining)) * TargetTraining;

    Edw = ((transpose(TargetValidation - (PhiValidation * Wml))) * (TargetValidation- (PhiValidation * Wml))) / 2;
    Eww = (transpose(Wml) * Wml) / 2;
    Ew = Edw + lamda * Eww;
    Erms = ((2 * Ew)/6963)^0.5;

end
