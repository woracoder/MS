function [errorPercent, reciprocalVal] = test_nn(W1, W2)

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

TestA = XTesting * W1;
TestZ = tanh(TestA);
TestZ = horzcat(ones([size(TestZ, 1), 1]), TestZ);

TestTmpY = TestZ * W2;
TestY = exp(TestTmpY);
[a, b] = size(TestY);

S = sum(TestY, 2);
for j=1:a
    for k=1:b
        TestY(j, k) = TestY(j, k)/S(i, 1);
    end
end

reciprocalVal = 0;
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

reciprocalVal = reciprocalVal/testErrorCount;
errorPercent = (testErrorCount * 100) / size(TestY, 1);

dlmwrite('classes_nn.txt', [XTesting(1:end, 2:end) TestY], ' ');

end