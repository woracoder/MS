function W = train_lr()

    %http://www.mathworks.com/help/matlab/ref/zeros.html
    Data = zeros(0, 512);
    TTraining = zeros(0, 10);

    %http://www.mathworks.com/help/matlab/ref/for.html
    for i=0:9

        %http://www.mathworks.com/help/matlab/import_export/import-data-interactively.html
        fileName = sprintf('feature/features_train/%d.txt', i);

        %http://www.mathworks.com/help/matlab/ref/importdata.html#btk7vi1-2_1
        dat = importdata(fileName, ' ', 0);
        Data = cat(1, Data, dat);

        %http://www.mathworks.com/help/matlab/ref/size.html
        a = size(dat, 1);
        tmp = zeros(a, 10);

        %http://www.mathworks.com/help/matlab/ref/ones.html
        tmp(1:a, i+1) = ones(a, 1);
        TTraining = cat(1, TTraining, tmp);

    end

    [m, n] = size(Data);

    XTraining = ones(m, n+1);
    XTraining(1:m, 2:n+1) = Data;

    %http://www.mathworks.com/help/matlab/ref/rand.html
    W = rand(513, 10);
    eta = 0.0001;
    errorLeast = 1000;

    for j=1:5000

        errorCount = 0;

        Aj = XTraining * W;

        %http://www.mathworks.com/help/matlab/ref/exp.html
        Y = exp(Aj);
        [a, b] = size(Y);

        %http://www.mathworks.com/help/matlab/ref/sum.html
        S = sum(Y, 2);
        for i=1:a
            for k=1:b
                Y(i, k) = Y(i, k)/S(i, 1);
            end
        end

        si = size(Y, 1);
        for l=1:si
            %http://www.mathworks.com/help/matlab/ref/max.html#bubk1h8-2
            [~, I] = max(Y(l,:));
            if TTraining(l, I) ~= 1
                errorCount = errorCount + 1;
            end
        end

        if errorCount < errorLeast
            errorLeast = errorCount;
            if errorLeast == 0
                break;
            end
        end

        %http://www.mathworks.com/help/matlab/ref/transpose.html
        DeltaEw = transpose(XTraining) * (Y - TTraining);

        W = W - (eta * DeltaEw);

    end

end