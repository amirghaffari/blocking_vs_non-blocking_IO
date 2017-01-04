
for /l %%x in (4, 1, 24) do (
  for /l %%y in (1, 1, 5) do (
     sbt "run %%x" > %%x_%%y.txt
  )
)
