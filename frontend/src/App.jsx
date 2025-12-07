import React, { useState, useEffect, useMemo } from 'react';
import { Moon, Sun, TrendingUp, Share2, AlertTriangle, CheckCircle } from 'lucide-react';
import TopicTrends from './components/charts/TopicTrends';
import TopicCooccurrences from './components/charts/TopicCooccurrences';
import MultithreadingPitfalls from './components/charts/MultithreadingPitfalls';
import QuestionSolvability from './components/charts/QuestionSolvability';

const App = () => {
  const [theme, setTheme] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return 'light';
  });

  const [activeSection, setActiveSection] = useState('trends');
  const [isManualScrolling, setIsManualScrolling] = useState(false);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    const handleChange = (e) => {
      setTheme(e.matches ? 'dark' : 'light');
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => (prev === 'light' ? 'dark' : 'light'));
  };

  const navItems = useMemo(() => [
    { id: 'trends', label: 'Topic Trends', icon: <TrendingUp size={20} /> },
    { id: 'cooccurrence', label: 'Co-occurrences', icon: <Share2 size={20} /> },
    { id: 'pitfalls', label: 'Concurrency Pitfalls', icon: <AlertTriangle size={20} /> },
    { id: 'solvability', label: 'Solvability Factors', icon: <CheckCircle size={20} /> },
  ], []);


  useEffect(() => {
    if (isManualScrolling) return;

    const handleIntersect = (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          setActiveSection(entry.target.id);
        }
      });
    };

    const observer = new IntersectionObserver(handleIntersect, {
      root: null,
      rootMargin: '-100px 0px -50% 0px', 
      threshold: 0
    });

    navItems.forEach((item) => {
      const element = document.getElementById(item.id);
      if (element) observer.observe(element);
    });

    return () => observer.disconnect();
  }, [navItems, isManualScrolling]);

  const scrollToSection = (id) => {
    setIsManualScrolling(true);
    setActiveSection(id);
    const element = document.getElementById(id);
    if (element) {
        const headerOffset = 80;
        const elementPosition = element.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
      
        window.scrollTo({
            top: offsetPosition,
            behavior: "smooth"
        });
        
      setTimeout(() => setIsManualScrolling(false), 1000);
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground transition-colors duration-300">
      <header className="fixed top-0 left-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="flex h-14 items-center justify-between px-8">
          
          <div className="mr-4 flex items-center gap-2 font-bold text-xl text-primary">
            <svg 
              viewBox="0 0 120 120" 
              className="h-8 w-8 fill-current text-orange-500"
              aria-hidden="true"
            >
              <path d="M84.4 93.8V70.6h7.7v30.9H22.6V70.6h7.7v23.2z" />
              <path d="M38.8 68.4l37.8 7.9 1.6-7.6-37.8-7.9-1.6 7.6zm5-18l35 16.3 3.2-7-35-16.4-3.2 7.1zm9.7-17.2l29.7 24.7 4.9-5.9-29.7-24.7-4.9 5.9zm19.2-18.3l-6.2 4.6 23 31 6.2-4.6-23-31zM38 86h38.6v-7.7H38V86z" />
            </svg>
             StackOverflow Java Stats
          </div>

          <button
            onClick={toggleTheme}
            className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors hover:bg-accent hover:text-accent-foreground h-10 w-10"
          >
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
          </button>
        </div>
      </header>

      <div className="flex w-full items-start pt-14">
        <aside className="fixed top-14 left-0 z-30 hidden h-[calc(100vh-3.5rem)] w-[220px] overflow-y-auto border-r bg-background md:block lg:w-[240px]">
          <nav className="grid items-start gap-2 p-4 text-sm font-medium">
            {navItems.map((item) => (
              <button
                key={item.id}
                onClick={() => scrollToSection(item.id)}
                className={`flex items-center gap-3 rounded-lg px-3 py-2 transition-all hover:text-primary ${
                  activeSection === item.id ? 'bg-muted text-primary' : 'text-muted-foreground'
                }`}
              >
                {item.icon}
                {item.label}
              </button>
            ))}
          </nav>
        </aside>

        <main className="flex-1 py-6 md:pl-[220px] lg:pl-[240px]">
          <div className="container px-4 md:px-8 max-w-6xl mx-auto">
            
            <section id="trends" className="mb-20 scroll-mt-24">
              <div className="space-y-4">
                <h2 className="text-3xl font-bold tracking-tight">Topic Trends</h2>
                <p className="text-muted-foreground">Analyze the popularity of Java topics over the last 12 months.</p>
                <div className="rounded-xl border bg-card text-card-foreground shadow">
                  <div className="p-6 pt-0 mt-6">
                    <TopicTrends />
                  </div>
                </div>
              </div>
            </section>

            <section id="cooccurrence" className="mb-20 scroll-mt-24">
              <div className="space-y-4">
                <h2 className="text-3xl font-bold tracking-tight">Topic Co-occurrences</h2>
                <p className="text-muted-foreground">Explore which tags frequently appear together in the Java ecosystem.</p>
                <div className="rounded-xl border bg-card text-card-foreground shadow">
                  <div className="p-6 pt-0 mt-6">
                    <TopicCooccurrences />
                  </div>
                </div>
              </div>
            </section>

            <section id="pitfalls" className="mb-20 scroll-mt-24">
              <div className="space-y-4">
                <h2 className="text-3xl font-bold tracking-tight">Multithreading Pitfalls</h2>
                <p className="text-muted-foreground">Distribution of common concurrency issues based on StackOverflow questions.</p>
                <div className="rounded-xl border bg-card text-card-foreground shadow">
                  <div className="p-6 pt-0 mt-6">
                    <MultithreadingPitfalls />
                  </div>
                </div>
              </div>
            </section>

            <section id="solvability" className="mb-20 scroll-mt-24">
              <div className="space-y-4">
                <h2 className="text-3xl font-bold tracking-tight">Question Solvability</h2>
                <p className="text-muted-foreground">What makes a question solvable vs hard-to-solve? A factor analysis.</p>
                <div className="rounded-xl border bg-card text-card-foreground shadow">
                  <div className="p-6 pt-0 mt-6">
                    <QuestionSolvability />
                  </div>
                </div>
              </div>
            </section>

          </div>
        </main>
      </div>
    </div>
  );
};

export default App;
