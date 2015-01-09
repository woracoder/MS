%{
Initial cleanup of the file Querylevelnorm.txt done by text editing tool EditPad Lite 7 by
doing a find and replacing with blank all the unnecessary text and saving it as a file cleandata.txt.
Regular expressions used for find replace are:
a) qid:(\d)*(\s)1:
b) (\s)#docid = (.)*
c) (\d)*:
%}

%{
Total rows      = 69623     (100% data)
Training rows   = 55700     (80% training data)
Validation rows = 6963      (10% validation data)
Testing rows    = 6960      (10% testing data)
%}

%http://www.mathworks.com/help/matlab/ref/importdata.html#btk7vi1-2_1
Data = importdata('cleandata.txt', ' ', 0);

trainingRows = 55700;
validationRows = 6963;
testingRows = 6960;

%http://www.mathworks.com/help/matlab/math/matrix-indexing.html#f1-85544
XTraining(1:55700, 1:46) = Data(1:55700, 2:47);
TargetTraining(1:55700, 1) = Data(1:55700, 1);

XValidation(1:6963, 1:46) = Data(55701:62663, 2:47);
TargetValidation(1:6963, 1) = Data(55701:62663, 1);

XTesting(1:6960, 1:46) = Data(62664:69623, 2:47);
TargetTesting(1:6960, 1) = Data(62664:69623, 1);

%http://www.mathworks.com/help/matlab/ref/cat.html
X = cat(1, XTraining, XValidation, XTesting);
Target = cat(1, TargetTraining, TargetValidation, TargetTesting);



%Linear Regaression starts here
%http://www.mathworks.com/help/matlab/ref/for.html
minLamda = 1;
maxLamda = 0;
minM = 1;
maxM = 0;
minErms = 1;
maxErms = 0;
innercount = 1;
outercount = 1;
plotdata = zeros(361, 3);
WmlBest = zeros(20, 1);
MuBest = zeros(1, 20);
sBest = zeros(46);

for M=2:20

    %http://www.mathworks.com/help/matlab/ref/mean.html
    MuTraining = zeros(1, M-1);
    for i=1:M-1
       XSection = XTraining(((i-1) * floor(55700/(M-1)) + 1):(i * floor(55700/(M-1))), 1:46);
       MuTraining(1, i) = mean(mean(XSection));
    end

    %http://www.mathworks.com/help/matlab/ref/var.html
    meanXTraining = mean(mean(XTraining));
    varianceTraining = 0.0;
    for j=1:55700
        for k=1:46
          varianceTraining = varianceTraining + ((XTraining(j, k) - meanXTraining) ^ 2);
        end
    end
    varianceTraining = varianceTraining/((55700*46)-1);

    %http://www.mathworks.com/help/matlab/ref/ones.html
    PhiTraining = zeros(55700, M);
    PhiTraining(1:55700, 1) = ones(55700, 1);

    %http://www.mathworks.com/help/matlab/ref/eye.html
    varianceTrainingMatrix = 2 * varianceTraining * eye(46);

    for n=1:55700
        XTrainingi = XTraining(n, 1:46);
        for m=1:M-1
            MuTrainingi = MuTraining(1, m) * ones(1,46);
            minusTrainingMat = XTrainingi-MuTrainingi;
            %http://www.mathworks.com/help/matlab/ref/transpose.html
            %Matlab suggested: Replace b*inv(A) with b/A
            tmpTrainingPhi = (minusTrainingMat/varianceTrainingMatrix) * transpose(minusTrainingMat);
            %http://www.mathworks.com/help/matlab/ref/exp.html
            PhiTraining(n, m+1) = exp(-tmpTrainingPhi);
        end
    end

     %Validation data code starts
    MuValidation = zeros(1, M-1);
    for i=1:M-1
       XValidationSection = XValidation(((i-1)*floor(6963/(M-1))+1):(i*floor(6963/(M-1))), 1:46);
       MuValidation(1, i) = mean(mean(XValidationSection));
    end

    meanXValidation = mean(mean(XValidation));
    varianceValidation = 0;
    for j=1:6963
        for k=1:46
          varianceValidation = varianceValidation + ((XValidation(j, k)-meanXValidation)^2);
        end
    end
    varianceValidation = varianceValidation/((6963*46)-1);

    PhiValidation = zeros(6963, M);
    PhiValidation(1:6963, 1) = ones(6963, 1);
    varianceValidationMatrix = 2 * varianceValidation * eye(46);
    for n=1:6963
        XValidationi = XValidation(n, 1:46);
        for m=1:M-1
            MuValidationi = MuValidation(1, m) * ones(1,46);
            minusValidationMat = XValidationi - MuValidationi;
            tmpValidationPhi = (minusValidationMat/varianceValidationMatrix) * transpose(minusValidationMat);
            PhiValidation(n, m+1) = exp(-tmpValidationPhi);
        end
    end
    
    for lamda=0.05:0.05:0.95
        %http://www.mathworks.com/help/matlab/matlab_prog/create-functions-in-files.html
        [Wml, Erms] = train_cfs(PhiTraining, TargetTraining, PhiValidation, TargetValidation, M, lamda);

        plotdata((innercount + ((outercount-1)*19)), 1) = M;
        plotdata((innercount + ((outercount-1)*19)), 2) = lamda;
        plotdata((innercount + ((outercount-1)*19)), 3) = Erms;
        
        if Erms < minErms
            minErms = Erms;
            minLamda = lamda;
            minM = M;
            WmlBest(1:M, 1) = Wml;
            MuBest = MuTraining;
            sBest = varianceTrainingMatrix;
        end

        if Erms > maxErms
            maxErms = Erms;
            maxLamda = lamda;
            maxM = M;
        end
        
        innercount = innercount + 1;
    end
    innercount = 1;
    outercount = outercount + 1;
end

finalErmsLR = test_cfs(XTesting, TargetTesting, minM, minLamda, WmlBest);

%Linear Regression ends here






%Stochastic Gradient Descent starts here
minMSGD = 1;
maxMSGD = 0;
minLamdaSGD = 1;
maxLamdaSGD = 0;
minErmsSGD = 1;
maxErmsSGD = 0;
inercount = 1;
otercount = 1;
plotdataSGD = zeros(361, 3);
WTauBest = zeros(M, 1);
MuBestSGD = zeros(1, 20);
sBestSGD = zeros(46);

for M=2:20
    
    MuTraining = zeros(1, M-1);
    for i=1:M-1
       XSection = XTraining(((i-1) * floor(55700/(M-1)) + 1):(i * floor(55700/(M-1))), 1:46);
       MuTraining(1, i) = mean(mean(XSection));
    end

    meanXTraining = mean(mean(XTraining));
    varianceTraining = 0.0;
    for j=1:55700
        for k=1:46
          varianceTraining = varianceTraining + ((XTraining(j, k) - meanXTraining) ^ 2);
        end
    end
    varianceTraining = varianceTraining/((55700*46)-1);

    PhiTraining = zeros(55700, M);
    PhiTraining(1:55700, 1) = ones(55700, 1);

    varianceTrainingMatrix = 2 * varianceTraining * eye(46);

    for n=1:55700
        XTrainingi = XTraining(n, 1:46);
        for m=1:M-1
            MuTrainingi = MuTraining(1, m) * ones(1,46);
            minusTrainingMat = XTrainingi-MuTrainingi;
            tmpTrainingPhi = (minusTrainingMat/varianceTrainingMatrix) * transpose(minusTrainingMat);
            PhiTraining(n, m+1) = exp(-tmpTrainingPhi);
        end
    end

    MuValidation = zeros(1, M-1);
    for i=1:M-1
       XValidationSection = XValidation(((i-1)*floor(6963/(M-1))+1):(i*floor(6963/(M-1))), 1:46);
       MuValidation(1, i) = mean(mean(XValidationSection));
    end

    meanXValidation = mean(mean(XValidation));
    varianceValidation = 0;
    for j=1:6963
        for k=1:46
          varianceValidation = varianceValidation + ((XValidation(j, k)-meanXValidation)^2);
        end
    end
    varianceValidation = varianceValidation/((6963*46)-1);

    PhiValidation = zeros(6963, M);
    PhiValidation(1:6963, 1) = ones(6963, 1);
    varianceValidationMatrix = 2 * varianceValidation * eye(46);
    
    for n=1:6963
        XValidationi = XValidation(n, 1:46);
        for m=1:M-1
            MuValidationi = MuValidation(1, m) * ones(1,46);
            minusValidationMat = XValidationi - MuValidationi;
            tmpValidationPhi = (minusValidationMat/varianceValidationMatrix) * transpose(minusValidationMat);
            PhiValidation(n, m+1) = exp(-tmpValidationPhi);
        end
    end
    
    for lamda=0.05:0.05:0.95
        [WTau, Erms] = train_gd(PhiTraining, TargetTraining, PhiValidation, TargetValidation, M, lamda);
        
        plotdataSGD((inercount + ((otercount-1)*19)), 1) = M;
        plotdataSGD((inercount + ((otercount-1)*19)), 2) = lamda;
        plotdataSGD((inercount + ((otercount-1)*19)), 3) = Erms;
    
        if Erms < minErmsSGD
            minErmsSGD = Erms;
            minMSGD = M;
            minLamdaSGD = lamda;
            WTauBest(1:M, 1) = WTau;
            MuBestSGD = MuTraining;
            sBestSGD = varianceTrainingMatrix;
        end

        if Erms > maxErmsSGD
            maxErmsSGD = Erms;
            maxMSGD = M;
            maxLamdaSGD = lamda;
        end
        inercount = inercount + 1;
    end
    
    inercount = 1;
    otercount = otercount + 1;
end

finalErmsSGD = test_gd(XTesting, TargetTesting, minMSGD, minLamdaSGD, WTauBest);

%Stochastic Gradient Descent ends here

fprintf('My ubit name is %s\n', 'srao2');
fprintf('My student number is %d\n', 50097976);
fprintf('the model complexity M_cfs is %d\n', minM);
fprintf('the model complexity M_gd is %d\n', minMSGD);
fprintf('the regularization parameters lambda_cfs is %4.2f\n', minLamda);
fprintf('the regularization parameters lambda_gd is %4.2f\n', minLamdaSGD);
fprintf('the root mean square error for the closed form solution is %4.2f\n', finalErmsLR);
fprintf('the root mean square error for the gradient descent method is %4.2f\n', finalErmsSGD);

%http://www.mathworks.com/help/matlab/ref/save.html
save 'project1_data.mat' Data;
save 'W_cfs.mat' WmlBest;
save 'W_gd.mat' WTauBest;
save 'mu_cfs.mat' MuBest;
save 'mu_gd.mat' MuBestSGD;
save 's_cfs.mat' sBest;
save 's_gd.mat' sBestSGD;

