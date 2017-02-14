-- (1) support functions
fac :: Integer -> Integer
fac n = if n == 0 then 1 else (n * fac(n-1))

fac' :: Integer -> [Integer]
fac' m = [ x | x <- [2 .. if m > 6 then (truncate (logBase 2 (fromIntegral m))) else m], fac x == m]

-- (1) main function
facInv :: Integer -> Integer
facInv m = if null x then -1 else (head x)
	where x = (fac' m)

---------------------------------------------------------

-- (2) main function
extractDigits :: String -> String
extractDigits s = [t | t <- s, elem t ['0' .. '9']]

---------------------------------------------------------

-- (3) support function
extractDigits' :: String -> String
extractDigits' s = [t | t <- s, elem t ['0' .. '9']]

-- (3) main function
convert :: String -> Integer
convert s = if null (extractDigits' s) then 0 else read (extractDigits' s)

---------------------------------------------------------

-- (4) support functions
isPrime' :: Int -> Int -> Bool
isPrime' num x
				| num == 0         = False
				| num == 1         = False
        		| x == num         = True
        		| (mod num x) == 0 = False
        		| otherwise        = isPrime' num (x + 1)

isPrime :: Int -> Bool
isPrime num = isPrime' num 2

checkForProperPrime :: String -> Int -> Int -> Bool
checkForProperPrime s n t = if ((read (take n (drop t s)) :: Integer) /= 0) && (length (show (read (take n (drop t s)) :: Integer)) == n) && (isPrime (read (take n (drop t s)))) then True else False 

findLeftMostPrime' :: String -> Int -> Int -> Integer
findLeftMostPrime' s n t
						 | (t == length s) || (n > (length s)) || (n == 0) = 0
						 | checkForProperPrime s n t 	   				   = read (take n (drop t s))
						 | otherwise				 	   				   = findLeftMostPrime' s n (t+1)

-- (4) main function
findLeftMostPrime :: String -> Int -> Integer
findLeftMostPrime s n = findLeftMostPrime' (extractDigits s) n 0

---------------------------------------------------------

-- (5) support function
findAllPrimes' :: String -> Int -> Int -> [Integer] -> [Integer]
findAllPrimes' s n t l
						 	| (n > (length s)) || (n == 0)  = []
						 	| (t == (length s))             = l
						 	| checkForProperPrime s n t     = findAllPrimes' s n (t+1) (l ++ [read (take n (drop t s))])
						 	| otherwise				 	   	= findAllPrimes' s n (t+1) l

-- (5) main function
findAllPrimes :: String -> Int -> [Integer]
findAllPrimes s n = findAllPrimes' (extractDigits s) n 0 []

---------------------------------------------------------