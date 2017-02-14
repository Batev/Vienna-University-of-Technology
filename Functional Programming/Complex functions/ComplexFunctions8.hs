---------------
-- Aufgabe 1 --
---------------

data GeladenerGast = A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T deriving (Eq,Ord,Enum,Show)

type Schickeria = [GeladenerGast] 					-- Aufsteigend geordnet
type Adabeis = [GeladenerGast] 						-- Aufsteigend geordnet
type Nidabeis = [GeladenerGast] 					-- Aufsteigend geordnet
type NimmtTeil = GeladenerGast -> Bool 				-- Total definiert
type Kennt = GeladenerGast -> GeladenerGast -> Bool -- Total definiert

istSchickeriaEvent :: NimmtTeil -> Kennt -> Bool
istSchickeriaEvent f g = length [t | t <- gaeste, elem t list && (f t)] /= 0
						 where list = schickeria f g

istSuperSchick :: NimmtTeil -> Kennt -> Bool
istSuperSchick f g = length [t | t <- gaeste, (elem t list) && (f t)] == 0
					 where list = adabeis f g

istVollProllig :: NimmtTeil -> Kennt -> Bool
istVollProllig f g = length [t | t <- gaeste, elem t list && (f t)] == 0
					 where list = schickeria f g

schickeria :: NimmtTeil -> Kennt -> Schickeria
schickeria f g = [v | v <- slebstGekannt, (gastKenntGaeste g v slebstGekannt []) == (gaesteKennenGast g slebstGekannt v [])] 
					where slebstGekannt = [t | t <- gaeste, (elem t (slebstKenenn g)) && not (elem t (nidabeis f g))]

adabeis :: NimmtTeil -> Kennt -> Adabeis
adabeis f g = [v | v <- gaeste, length (gastKenntGaeste g v schick []) == length schick]
				where schick = schickeria f g

nidabeis :: NimmtTeil -> Kennt -> Nidabeis
nidabeis f g = [t | t <- gaeste, not (f t)]

gaeste = [A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]

slebstKenenn :: Kennt -> [GeladenerGast]
slebstKenenn g = [t | t <- gaeste, g t t]

gastKenntGaeste :: Kennt -> GeladenerGast -> [GeladenerGast] -> [GeladenerGast] -> [GeladenerGast]
gastKenntGaeste kennt gast gaeste list
								| length gaeste == 0 = list
								| kennt gast curr    = gastKenntGaeste kennt gast (drop 1 gaeste) (list ++ [curr])
								| otherwise          = gastKenntGaeste kennt gast (drop 1 gaeste) list
								where curr = (head gaeste)

gaesteKennenGast :: Kennt -> [GeladenerGast] -> GeladenerGast -> [GeladenerGast] -> [GeladenerGast]
gaesteKennenGast kennt gaeste gast list
								| length gaeste == 0 = list
								| kennt curr gast    = gaesteKennenGast kennt (drop 1 gaeste) gast (list ++ [curr])
								| otherwise          = gaesteKennenGast kennt (drop 1 gaeste) gast list
								where curr = (head gaeste)

---------------
-- Aufgabe 2 --
---------------

stream :: [Integer]
stream = 1 : stream' (map (2*) stream) (stream' (map (3*) stream) (map (5*) stream))

stream' :: [Integer] -> [Integer] -> [Integer] 
stream' a@(x:xs) b@(y:ys) = case compare x y of
			            LT -> x : stream' xs b
			            EQ -> x : stream' xs ys
			            GT -> y : stream' a  ys

---------------
-- Aufgabe 3 --
---------------

type Quadrupel = (Integer,Integer,Integer,Integer)

quadrupel :: Integer -> [Quadrupel]
quadrupel n
			| n <= 0    = []
			| otherwise = (fil (gen n)) 

gen :: Integer -> [Quadrupel]
gen n = [(a,b,c,d) | a <- [1..n], b <- [1..n], c <- [1..n], d <- [1..n], a <= b && a < c && c <= d]

fil :: [Quadrupel] -> [Quadrupel]
fil q = [t | t <- q, fil' t]

fil' :: Quadrupel -> Bool
fil' (a,b,c,d) = (a^3+b^3) == (c^3+d^3)

sel :: Int -> [Quadrupel] -> Quadrupel
sel n q = q !! (abs n)