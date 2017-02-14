-- (1) support function
fac :: Integer -> Integer
fac n = if n == 0 then 1 else (n * fac(n-1))

-- (1) main functions
facLst :: Integer -> [Integer]
facLst n = if (n >= 0) then [fac t | t <- [0 .. n], t <= n] else []

factsL :: Integer -> [Integer]
factsL n = reverse (facLst n)

--------------------------------------------

-- (2) support functions
getNumbers :: String -> String
getNumbers s = [t | t <- s, elem t ['0' .. '9']]

extractNumerals' :: String -> Int -> [String] -> Bool -> [String]
extractNumerals' s t list bool
								| t == (length s) || (length s) == 0       = if bool then (list ++ [getNumbers (take (t) s)]) else list
								| elem (s !! t) ['0' .. '9'] && not bool   = extractNumerals' s (t+1) list True
								| not (elem (s !! t) ['0' .. '9']) && bool = extractNumerals' (drop t s) (0) (list ++ [getNumbers (take (t) s)]) False
								| otherwise                                = extractNumerals' s (t+1) list bool

-- (2) main function
extractNumerals :: String -> [String]
extractNumerals s = extractNumerals' s 0 [] False

--------------------------------------------

-- (3) support functions
powOf2' :: Int -> Int -> Int
powOf2' n k
			| (n == 1) && k /= 0 = k
			| mod n 2 /= 0       = -1
			| otherwise          = powOf2' (div n 2) (k+1)

powOf2 :: Int -> Int
powOf2 n 
		| n == 0    = -1
		| n == 1    = 0
		| otherwise = powOf2' n 0

isPowOf2' :: Int -> Bool
isPowOf2' n = ((powOf2 n) /= -1)

isInvalid :: String -> Bool
isInvalid t = length ([x | x <- t, elem x ['0' .. '9']]) /= (length t)

-- (3) main functions
isPowOf2 :: Int -> (Bool,Int)
isPowOf2 n = ((isPowOf2' n), (powOf2 n))

sL2pO2 :: [String] -> [Int]
sL2pO2 list = [powOf2 (read (if (isInvalid t) then "-1" else t) :: Int) | t <- list]

--------------------------------------------

-- (4) support functions
triMaxCurried :: Integer -> Integer -> Integer -> Integer
triMaxCurried p q r
			| (p>=q) && (p>=r) = p
			| (q>=p) && (q>=r) = q
			| (r>=p) && (r>=q) = r

triMaxUncurried :: (Integer,Integer,Integer) -> Integer
triMaxUncurried (p,q,r)
			| (p>=q) && (p>=r) = p
			| (q>=p) && (q>=r) = q
			| (r>=p) && (r>=q) = r

quadrAddCurried :: Integer -> Integer -> Integer -> Integer -> Integer
quadrAddCurried p q r t = p + q + r + t

quadrAddUncurried :: (Integer,Integer,Integer,Integer) -> Integer
quadrAddUncurried (p,q,r,t) = p + q + r + t

-- (4) main functions
curry3 :: ((a,b,c) -> d) -> a -> b -> c -> d
curry3 f p q r = f (p,q,r)

uncurry3 :: (a -> b -> c -> d) -> (a,b,c) -> d
uncurry3 f (p,q,r) = f p q r

curry4 :: ((a,b,c,d) -> e) -> a -> b -> c -> d -> e
curry4 f p q r t = f (p,q,r,t)

uncurry4 :: (a -> b -> c -> d -> e) -> (a,b,c,d) -> e
uncurry4 f (p,q,r,t) = f p q r t
--------------------------------------------