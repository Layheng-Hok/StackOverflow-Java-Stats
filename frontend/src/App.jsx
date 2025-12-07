import React, { useState, useEffect } from 'react';
import { Moon, Sun, TrendingUp, Share2, AlertTriangle, CheckCircle } from 'lucide-react';
import TopicTrends from './components/charts/TopicTrends';
import TopicCooccurrences from './components/charts/TopicCooccurrences';
import MultithreadingPitfalls from './components/charts/MultithreadingPitfalls';
import QuestionSolvability from './components/charts/QuestionSolvability';

const App = () => {
  const [theme, setTheme] = useState('light');
  const [activeSection, setActiveSection] = useState('trends');

  useEffect(() => {
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      setTheme('dark');
    }
  }, []);

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme(theme === 'light' ? 'dark' : 'light');
  };

  const navItems = [
    { id: 'trends', label: 'Topic Trends', icon: <TrendingUp size={20} /> },
    { id: 'cooccurrence', label: 'Co-occurrences', icon: <Share2 size={20} /> },
    { id: 'pitfalls', label: 'Concurrency Pitfalls', icon: <AlertTriangle size={20} /> },
    { id: 'solvability', label: 'Solvability Factors', icon: <CheckCircle size={20} /> },
  ];

  const scrollToSection = (id) => {
    setActiveSection(id);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground transition-colors duration-300">
      {/* Top Bar for Mobile / Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-14 items-center justify-between px-8">
          <div className="mr-4 flex font-bold text-xl text-primary">
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

      <div className="container flex-1 items-start md:grid md:grid-cols-[220px_1fr] md:gap-6 lg:grid-cols-[240px_1fr] lg:gap-10 p-8">
        
        {/* Left Navigation (Sticky) */}
        <aside className="fixed top-20 z-30 -ml-2 hidden h-[calc(100vh-3.5rem)] w-full shrink-0 overflow-y-auto border-r md:sticky md:block">
          <nav className="grid items-start gap-2 text-sm font-medium lg:px-4">
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

        {/* Main Content Area */}
        <main className="relative py-6 lg:gap-10 lg:py-8 xl:grid xl:grid-cols-[1fr]">
          <div className="w-full min-w-0">
            
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
