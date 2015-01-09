function [Erms] = test_cfs(XTesting, TargetTesting, M, lamda, Wml)

    WmlBest = Wml(1:M, 1);
    MuTesting = zeros(1, M-1);
    for i=1:M-1
       XSectionTesting = XTesting(((i-1) * floor(6960/(M-1)) + 1):(i * floor(6960/(M-1))), 1:46);
       MuTesting(1, i) = mean(mean(XSectionTesting));
    end
    
    meanXTesting = mean(mean(XTesting));
    varianceTesting = 0.0;
    for j=1:6960
        for k=1:46
          varianceTesting = varianceTesting + ((XTesting(j, k) - meanXTesting) ^ 2);
        end
    end
    varianceTesting = varianceTesting/((6960*46)-1);
    
    PhiTesting = zeros(6960, M);
    PhiTesting(1:6960, 1) = ones(6960, 1);

    varianceTestingMatrix = 2 * varianceTesting * eye(46);
    
    for n=1:6960
        XTestingi = XTesting(n, 1:46);
        for m=1:M-1
            MuTestingi = MuTesting(1, m) * ones(1,46);
            minusTestingMat = XTestingi-MuTestingi;
            tmpTestingPhi = (minusTestingMat/varianceTestingMatrix) * transpose(minusTestingMat);
            PhiTesting(n, m+1) = exp(-tmpTestingPhi);
        end
    end
   
    Edw = transpose(TargetTesting - (PhiTesting * WmlBest)) * (TargetTesting - (PhiTesting * WmlBest)) / 2;
    Eww = (transpose(WmlBest) * WmlBest) / 2;
    Ew = Edw + lamda * Eww;
    Erms = ((2 * Ew)/6960)^0.5;

end