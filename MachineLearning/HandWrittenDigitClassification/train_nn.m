function [W1, W2] = train_nn()

Data = zeros(0, 512);
TTraining = zeros(0, 10);
    
for i=0:9
    fileName = sprintf('feature/features_train/%d.txt', i);
    dat = importdata(fileName, ' ', 0);
    Data = cat(1, Data, dat);
    a = size(dat, 1);
    tmp = zeros(a, 10);
    tmp(1:a, i+1) = ones(a, 1);
    TTraining = cat(1, TTraining, tmp);
end
    
[m, n] = size(Data);
XTraining = ones(m, n+1);
XTraining(1:m, 2:n+1) = Data;

M = 100;
W1 = rand(513, M);
W1 = W1 - 0.5;
W2 = rand(M + 1, 10);
W2 = W2 - 0.5;
eta = 0.000001;
errorLeast = 25000;

for i=1:4000
    errorCount = 0;
    disp(i);
    A = XTraining * W1;
    Z = tanh(A);
    Z = horzcat(ones([size(Z, 1), 1]), Z);

    TmpY = Z * W2;
    Y = exp(TmpY);
    [a, b] = size(Y);

    S = sum(Y, 2);
    for j=1:a
        for k=1:b
            Y(j, k) = Y(j, k)/S(i, 1);
        end
    end
        
    si = size(Y, 1);
    for l=1:si
        [~, I] = max(Y(l,:));
        if TTraining(l, I) ~= 1
            errorCount = errorCount + 1;
        end
    end

    if errorCount < errorLeast
        errorLeast = errorCount;
        iteration = i;
        if errorLeast == 0
            break;
        end
    end
    
    DellEw2 = transpose(Y - TTraining) * Z;

    Z = Z(1:end, 2:end);
    tep = (Y - TTraining) * transpose(W2);
    tep = tep(1:end, 2:end);
    second = ones(size(Z)) - (Z.^2);
    DellEw1 = transpose(tep .* second) * XTraining;

    W2 = W2 - transpose(eta * DellEw2);
    W1 = W1 - (eta * transpose(DellEw1));

end
    
end