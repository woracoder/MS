function [errorPercent, reciprocalVal] = test_lr(W)

    %Work on testing data starts here
    TestingData = zeros(0, 512);
    TTesting = zeros(0, 10);
    for i=0:9
        fileName = sprintf('feature/features_test/%d.txt', i);
        dat = importdata(fileName, ' ', 0);
        TestingData = cat(1, TestingData, dat);
        a = size(dat, 1);
        tmp = zeros(a, 10);
        tmp(1:a, i+1) = ones(a, 1);
        TTesting = cat(1, TTesting, tmp);
    end

    [m, n] = size(TestingData);
    XTesting = ones(m, n+1);
    XTesting(1:m, 2:n+1) = TestingData;

    TestAj = XTesting * W;

    TestY = exp(TestAj);
    [a, b] = size(TestY);

    TestS = sum(TestY, 2);
    for i=1:a
        for k=1:b
            TestY(i, k) = TestY(i, k)/TestS(i, 1);
        end
    end

    reciprocalVal = 0;
    testErrorCount = 0;
    si = size(TestY, 1);
    for l=1:si
        [~, I] = max(TestY(l,:));
        if TTesting(l, I) ~= 1
            testErrorCount = testErrorCount + 1;
            val = TestY(l, I);
            Sorted = sort(TestY(l, :), 'descend');
            actVal = find(Sorted==val);
            reciprocalVal = reciprocalVal + (1/actVal);
        end
    end
    
    reciprocalVal = reciproalVal/testErrorCount;

    errorPercent = (testErrorCount * 100) / size(TestY, 1);

    dlmwrite('classes_lr.txt', [XTesting(1:end, 2:end) TestY], ' ');
    
end