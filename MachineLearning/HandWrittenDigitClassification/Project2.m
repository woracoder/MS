W = train_lr();
[errorPercent, reciprocalVal] = test_lr(W);

[W1, W2] = train_nn();
[errorPercent, reciprocalVal] = test_nn(W1, W2);