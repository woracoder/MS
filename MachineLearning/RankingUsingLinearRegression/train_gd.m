function [WTau, ErmsOldSGD] = train_gd(PhiTraining, TargetTraining, PhiValidation, TargetValidation, M, lamda)

    WTau = zeros(M, 1);
    eta = 1;
    PhiTrainingSGD = PhiTraining;
    ErmsOldSGD = 1;
    WTauOld = WTau;
    for z=1:400
        %http://www.mathworks.com/help/matlab/ref/randi.html
        rowNum = randi(55700, 1);
        PhiTrainingRowSGD = PhiTrainingSGD(rowNum, :);
        WTau = WTau + ((eta * (TargetTraining(rowNum, 1) - (PhiTrainingRowSGD * WTau))) * transpose(PhiTrainingRowSGD));

        EdwSGD = ((transpose(TargetValidation - (PhiValidation * WTau))) * (TargetValidation- (PhiValidation * WTau))) / 2;
        EwwSGD = (transpose(WTau) * WTau) / 2;
        EwSGD = EdwSGD + lamda * EwwSGD;
        ErmsNewSGD = ((2 * EwSGD)/6963)^0.5;

        %http://www.mathworks.com/help/matlab/ref/if.html
        if ErmsNewSGD < ErmsOldSGD
            ErmsOldSGD = ErmsNewSGD;
            WTauOld = WTau;
        else
            eta = eta * 0.9;
            WTau = WTauOld;
        end
    end

end